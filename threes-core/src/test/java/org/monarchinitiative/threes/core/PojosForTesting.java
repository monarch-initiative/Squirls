package org.monarchinitiative.threes.core;

import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

/**
 *
 */
public class PojosForTesting {

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
}
