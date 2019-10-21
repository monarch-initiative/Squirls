package org.monarchinitiative.threes.ingest;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;
import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

/**
 *
 */
public class PojosForTesting {

    private PojosForTesting() {
        // static utility class
    }

    public static SplicingTranscript makeAlphaTranscript() throws InvalidCoordinatesException {
        return SplicingTranscript.newBuilder()
                .setAccessionId("ACC_ALPHA")
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr2")
                        .setBegin(100)
                        .setEnd(900)
                        .setStrand(true)
                        .build())
                .addExon(SplicingExon.newBuilder()
                        .setBegin(100)
                        .setEnd(300)
                        .build())
                .addIntron(SplicingIntron.newBuilder()
                        .setBegin(300)
                        .setEnd(700)
                        .setDonorScore(2.345)
                        .setAcceptorScore(3.210)
                        .build())
                .addExon(SplicingExon.newBuilder()
                        .setBegin(700)
                        .setEnd(900)
                        .build())
                .build();
    }

    public static SplicingTranscript makeBetaTranscript() throws InvalidCoordinatesException {
        return SplicingTranscript.newBuilder()
                .setAccessionId("ACC_BETA")
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr3")
                        .setBegin(1000)
                        .setEnd(9000)
                        .setStrand(true)
                        .build())
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1000)
                        .setEnd(3000)
                        .build())
                .addIntron(SplicingIntron.newBuilder()
                        .setBegin(3000)
                        .setEnd(7000)
                        .setDonorScore(2.345)
                        .setAcceptorScore(3.210)
                        .build())
                .addExon(SplicingExon.newBuilder()
                        .setBegin(7000)
                        .setEnd(9000)
                        .build())
                .build();
    }


    public static TranscriptModel makeThreeExonTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval first = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 12_000);
        builder.addExonRegion(first);

        GenomeInterval second = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 14_000, 16_000);
        builder.addExonRegion(second);

        GenomeInterval third = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 18_000, 20_000);
        builder.addExonRegion(third);

        return builder.build();
    }

    public static TranscriptModel makeSingleExonTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval exon = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.addExonRegion(exon);
        return builder.build();
    }

    public static TranscriptModel makeSmallTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 200);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 120, 180);
        builder.setCDSRegion(cdsRegion);

        // the first and the last exons are really small
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 101));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 110, 160));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 170, 190));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 199, 200));
        return builder.build();
    }

    public static SequenceInterval getSequenceIntervalForTranscriptWithThreeExons() {
        return SequenceInterval.of(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(900)
                        .setEnd(2100)
                        .setStrand(true)
                        .build(),
                "AAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGTTACTAGTAAGCCTGATGCCACT" + // 960
                        "ACACAATTCTAGCTTTTCTCTTTAGGATGATTGTTTCATT" + // 1000
                        "CAGTCTTATCTCTTTTAGAA" + // 1020
                        "AACATAGGaaaaaattatttaataataaaatttaattGGCAAAATGAAGGTATGGCTTAT" + // 1080
                        "AAGAGTGTTTTCCTATTGTTTTCAGTGTAGGACTCACTGTTCTAAATAACTGGGACACCC" + // 1140
                        //                                      v  <- 1200  (end of the 0th exon)
                        "AAGGATTCTGTAAAATGCCATCCAGTTATCATTTATATTC" +
                        "CCTAACTCAAAATTCATTCA" +
                        "CATGTATTCATTTTTTTCTAAACAAATTAGCATGTAGAATTCTGGTTAAAATTTGGCATA" +
                        "GAACACCCGGGTATTTTTTCATAATGCACCCAATAACTGTCATTCACTAATTGAGAATGG" +
                        //                                                          v  <- 1400 (end of the 0th intron)
                        "TGATTTAACAAAGGATAATAAAGTTATGAAACCAATGCCACAAAACATCTGTCTCTAACT" +
                        "GgtgtgtgtgtgtgtgtgtgtgtgtgtgtgtgtAAGAGGGAGAGAGAGAAAATTTCACTC" +
                        "CCTCCATAAATCTCACAGTATTCTTTTCTTtttcctttcctttccttgctcttctttctc" +
                        "tcctattgctttcctttcatttccttCTCATAAAAGAAAAATAACAATATAGAAAATAAC" +
                        "AAAATATAGATGGTCAACCTTTTTAATATTAAGGTTACCTAAAATGCCATTATCCAAAGT" +
                        "GGTTCTCTAGAGATGCTGATGTATATACTTACATATTTTACAGTGTATTCAAATAAAGAG" +
                        "TATATTACATAAGACATATCCTTTTGTAACCAACTTTTGTCATTAACAATTTACTGGACT" +
                        "TGTCAACAAACCTAAATCTGTATCGTCTATAATGGCTACGTTCATTTTGGTATGAATCTT" +
                        "AATTACCCCTTTCTGCATTATTTAATGATTTTCTCATATGTCACTCTTAAATGTACTTCT" +
                        "AATTTTTCACTTTACATCACATAATGAATGGATCCAAATATGTTATGGATAGATATCTTC" +
                        "AAACTTTCTACTTACAAGTAGTGATAATAACAGATGTTCTCTCTAAAGTGTAGTTGGTAT" +
                        "CAATTTTACTGACCTTTAAAAATATCTTAATGGGACAAAGTTCAAATATTTGATGACCAG" +
                        "CTATCGTGACCTTTATCTCTGTGGCTCTGTGGGCCTGTAGTTTTTACGTGCTTTTAGTGT");
    }

    public static SplicingTranscript getTranscriptWithThreeExons() throws InvalidCoordinatesException {
        return SplicingTranscript.newBuilder()
                .setAccessionId("FIRST")
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1000)
                        .setEnd(2000)
                        .setStrand(true)
                        .build())
                // 1st exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1000)
                        .setEnd(1200)
                        .build())
                // 1st intron
                .addIntron(SplicingIntron.newBuilder()
                        .setBegin(1200)
                        .setEnd(1400)
                        .setDonorScore(5.555)
                        .setAcceptorScore(6.666)
                        .build())
                // 2nd exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1400)
                        .setEnd(1600)
                        .build())
                // 2nd intron
                .addIntron(SplicingIntron.newBuilder()
                        .setBegin(1600)
                        .setEnd(1800)
                        .setDonorScore(4.444)
                        .setAcceptorScore(3.333)
                        .build())
                // 3rd exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1800)
                        .setEnd(2000)
                        .build())
                .build();
    }

    public static SplicingTranscript getTranscriptWithSingleExons() throws InvalidCoordinatesException {
        return SplicingTranscript.newBuilder()
                .setAccessionId("FIRST")
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1000)
                        .setEnd(2000)
                        .setStrand(true)
                        .build())
                // single exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1000)
                        .setEnd(2000)
                        .build())
                .build();
    }

    public static SplicingTranscript getTranscriptWithThreeExonsOnRevStrand() throws InvalidCoordinatesException {
        return SplicingTranscript.newBuilder()
                .setAccessionId("FIRST")
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1000)
                        .setEnd(2000)
                        .setStrand(false)
                        .build())
                // 1st exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1000)
                        .setEnd(1200)
                        .build())
                // 1st intron
                .addIntron(SplicingIntron.newBuilder()
                        .setBegin(1200)
                        .setEnd(1400)
                        .setDonorScore(5.555)
                        .setAcceptorScore(6.666)
                        .build())
                // 2nd exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1400)
                        .setEnd(1600)
                        .build())
                // 2nd intron
                .addIntron(SplicingIntron.newBuilder()
                        .setBegin(1600)
                        .setEnd(1800)
                        .setDonorScore(4.444)
                        .setAcceptorScore(3.333)
                        .build())
                // 3rd exon
                .addExon(SplicingExon.newBuilder()
                        .setBegin(1800)
                        .setEnd(2000)
                        .build())
                .build();
    }

    public static SplicingParameters makeFakeSplicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(2).setDonorIntronic(3)
                .setAcceptorExonic(4).setAcceptorIntronic(5)
                .build();
    }

    /**
     * @return fake donor site represented by 4x5 matrix
     */
    public static DoubleMatrix makeFakeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                {.2, .3, .4, .3, .2}, // A
                {.1, .2, .3, .1, .6}, // C
                {.3, .3, .1, .3, .1}, // G
                {.4, .2, .2, .3, .1}  // T
        });
    }

    /**
     * @return fake acceptor site represented by 4x9 matrix
     */
    public static DoubleMatrix makeFakeAcceptorMatrix() {
        return new DoubleMatrix(new double[][]{
                {.2, .2, .4, .6, .1, .3, .4, .3, .2}, // A
                {.1, .2, .3, .2, .5, .2, .1, .6, .9}, // C
                {.3, .3, .1, .3, .1, .1, .2, .8, .2}, // G
                {.4, .2, .2, .3, .1, .8, .2, .1, .8}  // T
        });
    }

    /**
     * This matrix is the same matrix as the one in "spliceSites.yaml" YAML file.
     */
    public static DoubleMatrix makeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                // -3                  +1 (G) +2 (U)                +5     +6
                {0.332, 0.638, 0.097, 0.002, 0.001, 0.597, 0.683, 0.091, 0.179}, // A
                {0.359, 0.107, 0.028, 0.001, 0.012, 0.031, 0.079, 0.060, 0.152}, // C
                {0.186, 0.117, 0.806, 0.996, 0.001, 0.339, 0.122, 0.771, 0.191}, // G
                {0.123, 0.139, 0.069, 0.001, 0.986, 0.032, 0.116, 0.079, 0.478}  // T
        });
    }

    /**
     * This matrix is the same matrix as the one in "spliceSites.yaml" YAML file.
     */
    public static DoubleMatrix makeAcceptorMatrix() {
        return new DoubleMatrix(new double[][]{
                {0.248, 0.242, 0.238, 0.228, 0.215, 0.201, 0.181, 0.164, 0.147, 0.136, 0.125, 0.116, 0.106, 0.097, 0.090, 0.091, 0.102, 0.111, 0.117, 0.092, 0.096, 0.240, 0.063, 0.997, 0.001, 0.261, 0.251}, // A
                {0.242, 0.247, 0.250, 0.250, 0.253, 0.255, 0.263, 0.268, 0.271, 0.274, 0.281, 0.278, 0.274, 0.278, 0.259, 0.281, 0.292, 0.319, 0.330, 0.339, 0.293, 0.272, 0.644, 0.001, 0.002, 0.147, 0.191}, // C
                {0.159, 0.155, 0.154, 0.151, 0.151, 0.148, 0.148, 0.142, 0.138, 0.136, 0.128, 0.122, 0.116, 0.107, 0.102, 0.108, 0.114, 0.104, 0.093, 0.066, 0.066, 0.207, 0.006, 0.001, 0.996, 0.478, 0.194}, // G
                {0.350, 0.355, 0.358, 0.371, 0.381, 0.397, 0.409, 0.426, 0.444, 0.454, 0.466, 0.483, 0.504, 0.518, 0.549, 0.519, 0.491, 0.465, 0.460, 0.504, 0.545, 0.281, 0.287, 0.001, 0.001, 0.114, 0.365}  // T
        });
    }

    public static SplicingParameters makeSplicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(3)
                .setDonorIntronic(6)
                .setAcceptorExonic(2)
                .setAcceptorIntronic(25)
                .build();
    }

}
