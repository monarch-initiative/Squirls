package org.monarchinitiative.threes.ingest;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

/**
 *
 */
public class TestDataInstances {


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
}
