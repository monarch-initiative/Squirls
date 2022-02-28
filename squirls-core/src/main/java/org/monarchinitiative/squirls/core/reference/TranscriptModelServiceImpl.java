package org.monarchinitiative.squirls.core.reference;

import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.squirls.core.reference.jannovar.IntervalArray;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The service for serving transcripts stored in interval trees (in memory).
 */
class TranscriptModelServiceImpl implements TranscriptModelService {

    private final List<? extends Gene> genes;
    private final Map<String, Transcript> txByAccession;
    private final Map<String, IntervalArray<Transcript>> chromosomeMap;

    public static TranscriptModelServiceImpl of(List<? extends Gene> genes) {
        return new TranscriptModelServiceImpl(genes);
    }

    private static Map<String, IntervalArray<Transcript>> prepareIntervalArrays(List<? extends Gene> genes) {
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


    private TranscriptModelServiceImpl(List<? extends Gene> genes) {
        this.genes = Objects.requireNonNull(genes, "Genes must not be null");
        this.txByAccession = genes.stream()
                .flatMap(Gene::transcriptStream)
                .collect(Collectors.toUnmodifiableMap(Transcript::accession, Function.identity()));
        this.chromosomeMap = prepareIntervalArrays(genes);
    }

    @Override
    public Stream<? extends Gene> genes() {
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
