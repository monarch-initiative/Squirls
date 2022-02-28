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
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * The service for serving transcripts stored in interval trees (in memory).
 */
public class TranscriptModelServiceSg implements TranscriptModelService {

    private final List<Gene> genes;
    private final Map<String, Transcript> txByAccession;
    private final Map<String, IntervalArray<Transcript>> chromosomeMap;


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
        return new TranscriptModelServiceSg(genes);
    }

    private static Map<String, IntervalArray<Transcript>> prepareIntervalArrays(List<Gene> genes) {
        Map<String, List<Transcript>> geneByContig = genes.stream()
                .flatMap(Gene::transcriptStream)
                .collect(Collectors.groupingBy(g -> g.contig().genBankAccession()));

        Map<String, IntervalArray<Transcript>> chromosomeMap = new HashMap<>(geneByContig.keySet().size());
        for (String contig : geneByContig.keySet()) {
            List<Transcript> genesOnContig = geneByContig.get(contig);
            IntervalArray<Transcript> intervalArray = new IntervalArray<>(genesOnContig, TranscriptEndExtractor.instance());
            chromosomeMap.put(contig, intervalArray);
        }

        return Collections.unmodifiableMap(chromosomeMap);
    }


    private TranscriptModelServiceSg(List<Gene> genes) {
        this.genes = Objects.requireNonNull(genes, "Genes must not be null");
        this.txByAccession = genes.stream()
                .flatMap(Gene::transcriptStream)
                .collect(Collectors.toUnmodifiableMap(Transcript::accession, Function.identity()));
        this.chromosomeMap = prepareIntervalArrays(genes);
    }

    @Override
    public Stream<Gene> genes() {
        return genes.stream();
    }

    @Override
    public List<Transcript> overlappingTranscripts(GenomicRegion query) {
        IntervalArray<Transcript> intervalArray = chromosomeMap.get(query.contig().genBankAccession());
        if (intervalArray == null) {
            return List.of();
        }

        IntervalArray<Transcript>.QueryResult result = intervalArray.findOverlappingWithInterval(
                query.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased()),
                query.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased())
        );

        return Collections.unmodifiableList(result.getEntries());
    }

    @Override
    public Optional<Transcript> transcriptByAccession(String txAccession) {
        return Optional.ofNullable(txByAccession.get(txAccession));
    }

}
