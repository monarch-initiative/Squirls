package org.monarchinitiative.threes.core;

import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

/**
 *
 */
public class PojosForTesting {


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
