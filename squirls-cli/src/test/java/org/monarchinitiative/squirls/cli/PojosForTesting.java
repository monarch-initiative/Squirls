package org.monarchinitiative.squirls.cli;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class with static methods for construction of POJOs designed to be used for testing.
 */
public class PojosForTesting {

    public static final Random RANDOM = new Random(43);

    public static final double FAKE_THRESHOLD = .45;
    /**
     * A real sequence from interval `>chr9:136224501-136224800` (1-based coordinates) on hg19.
     */
    public static final String SEQUENCE = "TGTCCAGGGATGAGTCCAAGACACAGCCACCAGTCTGAATCCTTGCTGTGAACTGTCCCT" +
            "ACAAATTTGGTCTCTCTGCTCTGTAGGCACCAGTTGTTCTGCAAACTCACCCTGCGGCAC" +
            "ATCAACAAGTGCCCAGAACACGTGCTGAGGCACACCCAGGGCCGGCGGTACCAGCGAGCT" +
            "CTGTGTAAATGTAAGTCCCAGTGGACCCCCATCAGTGCATCGCCATCTGAGTGCATGCCC" +
            "GCCTTGCCCCAGATGGAGCGTGCTTGAAGGCAGGTCGTCCTTCAGCGATCCGTGTTGATG";

    private PojosForTesting() {
        // private no-op
    }

    public static SequenceInterval getSequence(ReferenceDictionary rd) {
        return SequenceInterval.builder()
                .sequence(SEQUENCE)
                .interval(new GenomeInterval(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136224501, 136224800, PositionType.ONE_BASED))
                .build();
    }

    public static SplicingVariantAlleleEvaluation getDonorPlusFiveEvaluation(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {
        final String chrom = "chr9";
        final int chr = 9;
        final int pos = 136_224_694;

        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("A", true);
        Allele altAlleleOne = Allele.create("T", false);
        Allele altAlleleTwo = Allele.create("TC", false);
        final VariantContext vc = new VariantContextBuilder()
                .chr(chrom) // on hg19
                .start(pos)
                .stop(pos)
                .id("rs993")
                .alleles(List.of(referenceAllele, altAlleleOne, altAlleleTwo))
                .make();

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, altAlleleOne);
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(chrom), pos, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "A", "T");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);
        evaluation.setAnnotations(ann);

        /*
        Make predictions map
         */
        final Map<String, SplicingPredictionData> predictions = PojosForTesting.surf2Transcripts(rd).stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, getSequence(rd)))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair(RANDOM.nextDouble(), FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(Metadata.builder()
                        .putDonorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, chr, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, chr, 136_224_587, PositionType.ONE_BASED))
                        .putDonorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, chr, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, chr, 136_224_587, PositionType.ONE_BASED))
                        .meanPhyloPScore(4.321)
                        .build()))
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));
        evaluation.putAllPredictionData(predictions);

        return evaluation;
    }

    public static SplicingVariantAlleleEvaluation getAcceptorMinusOneEvaluation(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {
        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("G", true);
        Allele alternateAllele = Allele.create("A", false);
        final VariantContext vc = new VariantContextBuilder()
                .chr("chr9")
                .start(136_224_586)
                .stop(136_224_586)
                .id("rs993")
                .alleles(List.of(referenceAllele, alternateAllele))
                .make();

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, alternateAllele);

        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136_224_586, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "A");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);
        evaluation.setAnnotations(ann);

        /*
        Make predictions map
         */
        final Map<String, SplicingPredictionData> predictions = PojosForTesting.surf2Transcripts(rd).stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, getSequence(rd)))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair(RANDOM.nextDouble(), FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(Metadata.builder()
                        .putDonorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, 9, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, 9, 136_224_587, PositionType.ONE_BASED))
                        .putDonorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, 9, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, 9, 136_224_587, PositionType.ONE_BASED))
                        .meanPhyloPScore(1.234)
                        .build()))
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));
        evaluation.putAllPredictionData(predictions);

        return evaluation;
    }

    public static Set<SplicingTranscript> surf2Transcripts(ReferenceDictionary rd) {
        return Set.of(surf2_NM_017503_4(rd), surf2_NM_001278928_1(rd));
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_017503.4</em>.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    public static SplicingTranscript surf2_NM_017503_4(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_017503.4")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136228034))
                // 1
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136223546))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223546, 136223789))
                        .setDonorScore(3.6156746223715936)
                        .setAcceptorScore(4.277366650982434)
                        .build())
                // 2
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223789, 136223944))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223944, 136224586))
                        .setDonorScore(2.937332682375464)
                        .setAcceptorScore(10.499414519258275)
                        .build())
                // 3
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224586, 136224690))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224690, 136226825))
                        .setDonorScore(9.136968204255682)
                        .setAcceptorScore(6.7796902152895875)
                        .build())
                // 4
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136226825, 136227005))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227005, 136227140))
                        .setDonorScore(6.3660441535158965)
                        .setAcceptorScore(8.610070990445257)
                        .build())
                // 5
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227140, 136227310))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227310, 136227931))
                        .setDonorScore(10.25048397144629)
                        .setAcceptorScore(10.042811633569952)
                        .build())
                // 6
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227931, 136228034))
                        .build())
                .build();
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_001278928.1</em>.
     * <p>
     * NOTE - according to Ensembl genome browser, the transcript
     * <a href="https://grch37.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;g=ENSG00000148291;r=9:136223428-136228045;t=ENST00000371964">ENST00000371964.4</a> corresponds to both RefSeq transcripts
     * <em>NM_001278928.1</em> and <em>NM_017503.4</em>.
     * </p>
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    public static SplicingTranscript surf2_NM_001278928_1(ReferenceDictionary rd) {
        // this is the same transcript as in the method surf2_NM_017503_4, except for the accession ID
        return SplicingTranscript.builder()
                .setAccessionId("NM_001278928.1")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136228034))
                // 1
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136223546))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223546, 136223789))
                        .setDonorScore(3.6156746223715936)
                        .setAcceptorScore(4.277366650982434)
                        .build())
                // 2
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223789, 136223944))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223944, 136224586))
                        .setDonorScore(2.937332682375464)
                        .setAcceptorScore(10.499414519258275)
                        .build())
                // 3
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224586, 136224690))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224690, 136226825))
                        .setDonorScore(9.136968204255682)
                        .setAcceptorScore(6.7796902152895875)
                        .build())
                // 4
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136226825, 136227005))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227005, 136227140))
                        .setDonorScore(6.3660441535158965)
                        .setAcceptorScore(8.610070990445257)
                        .build())
                // 5
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227140, 136227310))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227310, 136227931))
                        .setDonorScore(10.25048397144629)
                        .setAcceptorScore(10.042811633569952)
                        .build())
                // 6
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227931, 136228034))
                        .build())
                .build();
    }

}
