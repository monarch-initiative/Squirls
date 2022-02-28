package org.monarchinitiative.squirls.cli.cmd;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.Chromosome;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.impl.intervals.IntervalArray;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.sgenes.model.CodingTranscript;
import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for creating {@link ReferenceDictionary} and {@link VariantAnnotator} from a stream of {@link Gene}s.
 */
class JannovarUtil {

    private JannovarUtil() {
    }

    static ReferenceDictionary createReferenceDictionary(GenomicAssembly assembly) {
        ReferenceDictionaryBuilder builder = new ReferenceDictionaryBuilder();
        for (Contig contig : assembly.contigs()) {
            builder.putContigID(contig.name(), contig.id());
            builder.putContigID(contig.refSeqAccession(), contig.id());
            builder.putContigID(contig.genBankAccession(), contig.id());
            builder.putContigID(contig.ucscName(), contig.id());

            builder.putContigName(contig.id(), contig.name());

            builder.putContigLength(contig.id(), contig.length());
        }

        return builder.build();
    }

    static VariantAnnotator createVariantAnnotator(ReferenceDictionary rd, Stream<Gene> genes) {
        ImmutableMap<Integer, Chromosome> chromosomes = createChromosomes(rd, genes);
        return new VariantAnnotator(rd, chromosomes, new AnnotationBuilderOptions());
    }

    private static ImmutableMap<Integer, Chromosome> createChromosomes(ReferenceDictionary rd, Stream<Gene> genes) {
        Map<Integer, List<TranscriptModel>> txByContigId = genes.parallel()
                .flatMap(toTranscriptModels(rd))
                .collect(Collectors.groupingBy(TranscriptModel::getChr));

        ImmutableMap.Builder<Integer, Chromosome> builder = ImmutableMap.builder();
        for (Map.Entry<Integer, List<TranscriptModel>> e : txByContigId.entrySet()) {
            Integer contigId = e.getKey();
            List<TranscriptModel> transcripts = e.getValue();

            IntervalArray<TranscriptModel> array = new IntervalArray<>(transcripts, new TranscriptIntervalEndExtractor());
            builder.put(contigId, new Chromosome(rd, contigId, array));
        }

        return builder.build();
    }

    private static Function<Gene, Stream<TranscriptModel>> toTranscriptModels(ReferenceDictionary rd) {
        return gene -> gene.transcriptStream()
                .map(tx -> {
                    TranscriptModelBuilder builder = new TranscriptModelBuilder();
                    // identifiers
                    builder.setGeneID(gene.accession());
                    builder.setGeneSymbol(gene.symbol());
                    builder.setAccession(tx.accession());

                    Strand strand = mapStrand(tx.strand());
                    builder.setStrand(strand);
                    GenomeInterval txInterval = getTxRegion(rd, tx, strand);
                    builder.setTXRegion(txInterval);


                    builder.setCDSRegion(getCdsRegion(rd, tx, txInterval));

                    for (Coordinates exon : tx.exons()) {
                        builder.addExonRegion(new GenomeInterval(rd,
                                strand,
                                tx.contigId(),
                                exon.startWithCoordinateSystem(CoordinateSystem.zeroBased()),
                                exon.endWithCoordinateSystem(CoordinateSystem.zeroBased()),
                                PositionType.ZERO_BASED));
                    }

                    return builder.build();
                });
    }

    private static GenomeInterval getTxRegion(ReferenceDictionary rd, Transcript tx, Strand strand) {
        return new GenomeInterval(rd,
                strand,
                tx.contigId(),
                tx.startWithCoordinateSystem(CoordinateSystem.zeroBased()),
                tx.endWithCoordinateSystem(CoordinateSystem.zeroBased()),
                PositionType.ZERO_BASED);
    }

    private static GenomeInterval getCdsRegion(ReferenceDictionary rd, Transcript tx, GenomeInterval txInterval) {
        if (tx instanceof CodingTranscript) {
            Coordinates cds = ((CodingTranscript) tx).cdsCoordinates();
            return new GenomeInterval(rd,
                    txInterval.getStrand(),
                    tx.contigId(),
                    cds.startWithCoordinateSystem(CoordinateSystem.zeroBased()),
                    cds.endWithCoordinateSystem(CoordinateSystem.zeroBased()),
                    PositionType.ZERO_BASED);
        } else {
            return new GenomeInterval(txInterval.getGenomeBeginPos(), 0);
        }

    }

    private static Strand mapStrand(org.monarchinitiative.svart.Strand strand) {
        switch (strand) {
            case POSITIVE:
                return Strand.FWD;
            case NEGATIVE:
                return Strand.REV;
            default:
                throw new RuntimeException("Illegal strand: `" + strand + "`");
        }
    }

}
