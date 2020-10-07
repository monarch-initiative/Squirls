package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.jblas.DoubleMatrix;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;

import java.util.Map;
import java.util.Random;

/**
 * Class with static method for construction of medium-complicated objects.
 */
public class PojosForTesting {

    private static final String THREE_EXON_TX_SEQ = // upstream 100bp
            "AAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGTTACTAGTAAGCCTGATGCCACT" + // 960
                    "ACACAATTCTAGCTTTTCTCAGAGCCCCGCCCCCGGCTCC" + // 1000
                    //
                    // 1st exon
                    "AGGTTCTGCGAGCGGCTTCCAACATAGGaaaaaattatttaataataaaatttaattGGC" +
                    "AAAATGAAGGTATGGCTTATAAGAGTGTTTTCCTATTGTTTTCAGTGTAGGACTCACTGT" +
                    "TCTAAATAACTGGGACACCCAAGGATTCTGAGCCTGCGGCTCCAGACGGACGCCCGCAAG" +
                    "TCCAGACGGACGCCCGCAAG" + // 1200 (end of the 1st exon)
                    //
                    // 1st intron
                    "gttcgcagcgcgggaggggaacggagtggcggaGTAGAATTCTGGTTAAAATTTGGCATA" +
                    "GAACACCCGGGTATTTTTTCATAATGCACCCAATAACTGTCATTCACTAATTGAGAATGG" +
                    "TGATTTAACAAAGGATAATAAAGTTATGAActgacgtcctcctggccctcctgacgtcct" +
                    "gcccgcccacgcgtccgcag" +
                    //                  ^  <- 1400 (end of the 1st intron)
                    // 2nd exon
                    "GTGAGGTGCATCCTGACAGGTCACGAGCTGCCCTtcactcCCTCCATAAATCTCACAGTA" +
                    "TTCTTTTCTTtttcctttcctttccttgctcttctttctctcctattgctttcctttcat" +
                    "ttccttCTCATAAAAGAAAAATAACAATATAGAAAATAACAAAATATAGATGGTCAACCT" +
                    "GTGCCCAGCACCAAGAACCC" +
                    //                  ^  <- 1600 (end of the 2nd exon)
                    // 2nd intron
                    "gtaggtggtccgcggcggcgcggggaggcccaggGCTGATGTATATACTTACATATTTTA" +
                    "CAGTGTATTCAAATAAAGAGTATATTACATAAGACATATCCTTTTGTAACCAACTTTTGT" +
                    "CATTAACAATTTACTGGACTTGTCAACAAACCTAAATCTGtgtgaactgtccctacaaat" +
                    "ttggtctctctgctctgtag" +
                    //                  ^  <- 1800 (end of the 2nd intron)
                    // 3rd exon
                    "GCACCAGTTGTTCTGCAAACTCACCCTGCGGCACATCAACAAGTGCCCAGAACACTGTCT" +
                    "aatttttcacTTTACATCACATAATGAATGGATCCAAATATGTTATGGATAGATATCTTC" +
                    "AAACTTTCTACTTACAAGTAGTGATAATAACAGATGTTCTCTCTAAAGTGTAGTTGGTAT" +
                    "CCAGCGAGCTCTGTGTAAAT" +
                    //                  ^  <- 2000 (end of the 3rd exon)
                    // downstream 100bp
                    "AATATCTTAATGGGACAAAGTTCAAATATTTGATGACCAGCTATCGTGACCTTTATCTCT" +
                    "GTGGCTCTGTGGGCCTGTAGTTTTTACGTGCTTTTAGTGT";

    private PojosForTesting() {
        // static utility class
    }

    public static SequenceRegion getSequenceIntervalForTranscriptWithThreeExons(ReferenceDictionary referenceDictionary) {
        return SequenceRegion.of(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 900, 2100), THREE_EXON_TX_SEQ);
    }

    public static SplicingTranscript getTranscriptWithThreeExons(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000))
                // 1st exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 1200))
                        .build())
                // 1st intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1200, 1400))
                        .setDonorScore(5.555)
                        .setAcceptorScore(6.666)
                        .build())
                // 2nd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1400, 1600))
                        .build())
                // 2nd intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1600, 1800))
                        .setDonorScore(4.444)
                        .setAcceptorScore(3.333)
                        .build())
                // 3rd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1800, 2000))
                        .build())
                .build();
    }

    public static SplicingTranscript getTranscriptWithSingleExon(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000))
                // single exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000))
                        .build())
                .build();
    }

    public static SplicingTranscript getTranscriptWithThreeExonsOnRevStrand(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000).withStrand(Strand.REV))

                // 1st exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1800, 2000).withStrand(Strand.REV))
                        .build())
                // 1st intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1600, 1800).withStrand(Strand.REV))
                        .setDonorScore(4.444)
                        .setAcceptorScore(3.333)
                        .build())
                // 2nd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1400, 1600).withStrand(Strand.REV))
                        .build())
                // 2nd intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1200, 1400).withStrand(Strand.REV))
                        .setDonorScore(5.555)
                        .setAcceptorScore(6.666)
                        .build())
                // 3rd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 1200).withStrand(Strand.REV))
                        .build())
                .build();
    }


    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_017503.5</em>.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    public static SplicingTranscript surf2_NM_017503_5(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_017503.5")
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

    // This matrix is expected to be read from the test YAML file at "/genome/dao/splicing/spliceSites.yaml"
    public static DoubleMatrix makeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                // -3                  +1 (G) +2 (U)                +5     +6
                {0.332, 0.638, 0.097, 0.002, 0.001, 0.597, 0.683, 0.091, 0.179}, // A
                {0.359, 0.107, 0.028, 0.001, 0.012, 0.031, 0.079, 0.060, 0.152}, // C
                {0.186, 0.117, 0.806, 0.996, 0.001, 0.339, 0.122, 0.771, 0.191}, // G
                {0.123, 0.139, 0.069, 0.001, 0.986, 0.032, 0.116, 0.079, 0.478}  // T
        });
    }


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

    public static Map<String, TrackRegion<?>> getTrackMap(ReferenceDictionary rd) {
        GenomeInterval interval = new GenomeInterval(rd, Strand.FWD, 1, 900, 2100);

        final Random random = new Random(123);
        final float[] fakePhylop = new float[1200];
        for (int i = 0; i < 1200; i++) {
            fakePhylop[i] = random.nextFloat();
        }

        return Map.of(
                "fasta", SequenceRegion.of(interval, THREE_EXON_TX_SEQ),
                "phylop", FloatRegion.of(interval, fakePhylop)
        );
    }
}
