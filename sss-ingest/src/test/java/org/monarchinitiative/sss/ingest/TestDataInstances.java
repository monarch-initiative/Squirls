package org.monarchinitiative.sss.ingest;

import org.monarchinitiative.sss.core.model.GenomeInterval;
import org.monarchinitiative.sss.core.model.SplicingExon;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingTranscript;

/**
 *
 */
public class TestDataInstances {


    public static SplicingTranscript makeAlphaTranscript() {
        return SplicingTranscript.newBuilder()
                .setAccessionId("ACC_ALPHA")
                .setInterval(GenomeInterval.newBuilder()
                        .setContig("chr2")
                        .setBegin(100)
                        .setEnd(900)
                        .setStrand(true)
                        .setContigLength(100_000)
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

    public static SplicingTranscript makeBetaTranscript() {
        return SplicingTranscript.newBuilder()
                .setAccessionId("ACC_BETA")
                .setInterval(GenomeInterval.newBuilder()
                        .setContig("chr3")
                        .setBegin(1000)
                        .setEnd(9000)
                        .setStrand(true)
                        .setContigLength(200_000)
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
}
