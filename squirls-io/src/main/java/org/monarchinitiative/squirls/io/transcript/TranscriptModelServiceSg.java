package org.monarchinitiative.squirls.io.transcript;

import org.monarchinitiative.sgenes.io.GeneParser;
import org.monarchinitiative.sgenes.io.GeneParserFactory;
import org.monarchinitiative.sgenes.io.SerializationFormat;
import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.squirls.core.reference.TranscriptModelService;
import org.monarchinitiative.squirls.io.SquirlsResourceException;
import org.monarchinitiative.squirls.io.transcript.jannovar.IntervalArray;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.assembly.GenomicAssembly;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * The service for serving transcripts stored in interval trees (in memory).
 */
public class TranscriptModelServiceSg implements TranscriptModelService {

    private final Map<String, Transcript> txByAccession;
    private final List<String> txAccessions;
    private final Map<String, IntervalArray<Gene>> chromosomeMap;


    public static TranscriptModelService of(GenomicAssembly assembly, Path silentGenesJson) throws SquirlsResourceException {
        Objects.requireNonNull(assembly, "Assembly must not be null");
        Objects.requireNonNull(silentGenesJson, "Genes JSON path must not be null");

        GeneParserFactory parserFactory = GeneParserFactory.of(assembly);
        GeneParser parser = parserFactory.forFormat(SerializationFormat.JSON);

        try (InputStream is = new BufferedInputStream(new GZIPInputStream(Files.newInputStream(silentGenesJson)))){
            List<Gene> genes = (List<Gene>) parser.read(is);
            return of(genes);
        } catch (IOException e) {
            throw new SquirlsResourceException("Error occurred while reading file `" + silentGenesJson.toAbsolutePath() + "`", e);
        }
    }

    public static TranscriptModelServiceSg of(List<Gene> genes) {
        Objects.requireNonNull(genes, "Genes must not be null");
        List<String> txAccessions = genes.stream()
                .flatMap(Gene::transcriptStream)
                .map(Transcript::accession)
                .collect(Collectors.toUnmodifiableList());

        Map<String, Transcript> txByAccession = genes.stream()
                .flatMap(Gene::transcriptStream)
                .collect(Collectors.toUnmodifiableMap(Transcript::accession, Function.identity()));

        Map<String, IntervalArray<Gene>> chromosomeMap = prepareIntervalArrays(genes);

        return new TranscriptModelServiceSg(txAccessions, txByAccession, chromosomeMap);
    }

    private static Map<String, IntervalArray<Gene>> prepareIntervalArrays(List<Gene> genes) {
        Map<String, Set<Gene>> geneByContig = genes.stream()
                .collect(Collectors.groupingBy(g -> g.contig().genBankAccession(), Collectors.toUnmodifiableSet()));

        Map<String, IntervalArray<Gene>> chromosomeMap = new HashMap<>(geneByContig.keySet().size());
        for (String contig : geneByContig.keySet()) {
            Set<Gene> genesOnContig = geneByContig.get(contig);
            IntervalArray<Gene> intervalArray = new IntervalArray<>(genesOnContig, GeneEndExtractor.instance());
            chromosomeMap.put(contig, intervalArray);
        }

        return Collections.unmodifiableMap(chromosomeMap);
    }


    private TranscriptModelServiceSg(List<String> txAccessions,
                                     Map<String, Transcript> txByAccession,
                                     Map<String, IntervalArray<Gene>> chromosomeMap) {
        this.txAccessions = txAccessions;
        this.txByAccession = txByAccession;
        this.chromosomeMap = chromosomeMap;
    }


    @Override
    public List<String> getTranscriptAccessionIds() {
        return txAccessions;
    }

    @Override
    public List<Transcript> overlappingTranscripts(GenomicRegion query) {
        IntervalArray<Gene> intervalArray = chromosomeMap.get(query.contig().genBankAccession());
        if (intervalArray == null) {
            return List.of();
        }

        IntervalArray<Gene>.QueryResult result = intervalArray.findOverlappingWithInterval(
                query.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased()),
                query.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased())
        );

        return result.getEntries().stream()
                .flatMap(Gene::transcriptStream)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Transcript> transcriptByAccession(String txAccession) {
        return Optional.ofNullable(txByAccession.get(txAccession));
    }

}
