package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.*;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;
import org.monarchinitiative.sss.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.sss.core.reference.InvalidCoordinatesException;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;

import java.util.Optional;

/**
 *
 */
public class NaiveSplicingTranscriptLocator implements SplicingTranscriptLocator {

    private final SplicingParameters parameters;

    private final GenomeCoordinatesFlipper flipper;

    public NaiveSplicingTranscriptLocator(SplicingParameters parameters, GenomeCoordinatesFlipper flipper) {
        this.parameters = parameters;
        this.flipper = flipper;
    }


    private static boolean overlapsWith(GenomeCoordinates first, GenomeCoordinates second) {
        return second.getBegin() < first.getEnd() && first.getBegin() < second.getEnd();
    }

    @Override
    public SplicingLocationData localize(SplicingVariant variant, SplicingTranscript transcript) {
        try {
            final SplicingLocationData.Builder dataBuilder = SplicingLocationData.newBuilder();

            GenomeCoordinates coordinates = transcript.getCoordinates();
            // on the same contig
            if (!variant.getContig().equals(coordinates.getContig())) {
                return SplicingLocationData.outside();
            }

            // ensure tx coordinates are on FWD strand
            if (!coordinates.isStrand()) {
                final Optional<GenomeCoordinates> op = flipper.flip(coordinates);
                if (op.isEmpty()) {
                    return SplicingLocationData.outside();
                }
                coordinates = op.get();
            }

            // intersects with transcript
            final GenomeCoordinates varCoor = variant.getCoordinates();
            if (!overlapsWith(coordinates, varCoor)) {
                return SplicingLocationData.outside();
            }

            // is this a single exon gene?
            if (transcript.getExons().size() == 1) {
                // nothing more to be solved, variant intersects with transcript as checked above.
                // SplicingPosition must be EXON
                return dataBuilder
                        .setFeatureIndex(0)
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                        .build();
            }

            for (int i = 0; i < transcript.getIntrons().size(); i++) {
                final SplicingExon exon = transcript.getExons().get(i);
                final SplicingIntron intron = transcript.getIntrons().get(i);

                // 1 - is the variant in the donor site?
                // donor
                GenomeCoordinates donor = GenomeCoordinates.newBuilder()
                        .setContig(transcript.getContig())
                        .setBegin(intron.getBegin() - parameters.getDonorExonic())
                        .setEnd(intron.getBegin() + parameters.getDonorIntronic())
                        .setStrand(transcript.getStrand())
                        .build();

                if (overlapsWith(donor, varCoor)) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                            .setFeatureIndex(i)
                            .build();
                }

                // 2 - is the variant in the acceptor site?
                // acceptor
                GenomeCoordinates acceptor = GenomeCoordinates.newBuilder()
                        .setContig(transcript.getContig())
                        .setBegin(intron.getEnd() - parameters.getAcceptorIntronic())
                        .setEnd(intron.getEnd() + parameters.getAcceptorExonic())
                        .setStrand(transcript.getStrand())
                        .build();

                if (overlapsWith(acceptor, varCoor)) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                            .setFeatureIndex(i)
                            .build();
                }


                // 3 - does the variant overlap with the current intron?
                if (intron.getBegin() < varCoor.getEnd() && varCoor.getBegin() < intron.getEnd()) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.INTRON)
                            .setFeatureIndex(i)
                            .build();
                }

                // 4 - does the variant overlap with the current exon?
                if (exon.getBegin() < varCoor.getEnd() && varCoor.getBegin() < exon.getEnd()) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                            .setFeatureIndex(i)
                            .build();
                }
            }

            // For transcript with x exons we processed x-1 exons and x introns above. There is no overlap with previous
            // exons/introns/canonical splice sites if we get here. Since the variant overlaps with the transcript, it
            // has to overlap with the last exon.
            final int lastExonIdx = transcript.getExons().size() - 1;
            return dataBuilder
                    .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                    .setFeatureIndex(lastExonIdx)
                    .build();

        } catch (InvalidCoordinatesException e) {
            return SplicingLocationData.outside();
        }
    }
}
