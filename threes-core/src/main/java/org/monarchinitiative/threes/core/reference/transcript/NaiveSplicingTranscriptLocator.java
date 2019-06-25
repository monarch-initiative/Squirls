package org.monarchinitiative.threes.core.reference.transcript;

import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.pwm.SplicingParameters;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

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

    @Override
    public SplicingLocationData locate(SplicingVariant variant, SplicingTranscript transcript) {
        try {
            final SplicingLocationData.Builder dataBuilder = SplicingLocationData.newBuilder();

            GenomeCoordinates txCoordinates = transcript.getTxRegionCoordinates();
            GenomeCoordinates varCoordinates = variant.getCoordinates();
            // on the same contig
            if (!varCoordinates.getContig().equals(txCoordinates.getContig())) {
                return SplicingLocationData.outside();
            }

            // Variant coordinates are flipped to transcript's strand.
            if (txCoordinates.isStrand() != varCoordinates.isStrand()) {
                final Optional<GenomeCoordinates> op = flipper.flip(varCoordinates);
                if (!op.isPresent()) {
                    return SplicingLocationData.outside();
                }
                varCoordinates = op.get();
            }

            // return outside if variant does not intersect with transcript
            if (!txCoordinates.overlapsWith(varCoordinates)) {
                return SplicingLocationData.outside();
            }

            // is this a single exon gene?
            if (transcript.getExons().size() == 1) {
                // nothing more to be solved, variant intersects with transcript as checked above.
                // SplicingPosition must be EXON
                return dataBuilder
                        .setExonIndex(0)
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

                if (donor.overlapsWith(varCoordinates)) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                            .setIntronIndex(i)
                            .setExonIndex(i)
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

                if (acceptor.overlapsWith(varCoordinates)) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                            .setIntronIndex(i)
                            .setExonIndex(i + 1)
                            .build();
                }


                // 3 - does the variant overlap with the current intron?
                if (intron.getBegin() < varCoordinates.getEnd() && varCoordinates.getBegin() < intron.getEnd()) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.INTRON)
                            .setIntronIndex(i)
                            .build();
                }

                // 4 - does the variant overlap with the current exon?
                if (exon.getBegin() < varCoordinates.getEnd() && varCoordinates.getBegin() < exon.getEnd()) {
                    return dataBuilder
                            .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                            .setExonIndex(i)
                            .build();
                }
            }

            // For transcript with x exons we processed x-1 exons and x introns above. There is no overlap with previous
            // exons/introns/canonical splice sites if we get here. Since the variant overlaps with the transcript, it
            // has to overlap with the last exon.
            final int lastExonIdx = transcript.getExons().size() - 1;
            return dataBuilder
                    .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                    .setExonIndex(lastExonIdx)
                    .build();

        } catch (InvalidCoordinatesException e) {
            return SplicingLocationData.outside();
        }
    }
}
