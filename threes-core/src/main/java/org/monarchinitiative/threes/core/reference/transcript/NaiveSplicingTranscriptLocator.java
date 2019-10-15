package org.monarchinitiative.threes.core.reference.transcript;

import org.monarchinitiative.threes.core.model.*;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;

/**
 * This class figures out where exactly the variant is located with respect to given <code>transcript</code>.
 * <p>
 * The variant is considered as:
 * <ul>
 * <li>{@link SplicingLocationData.SplicingPosition#DONOR} if the variant overlaps with any <em>donor</em> site of
 * the <code>transcript</code></li>
 * <li>{@link SplicingLocationData.SplicingPosition#ACCEPTOR} if the variant overlaps with any <em>acceptor</em> site
 * of the <code>transcript</code></li>
 * <li>{@link SplicingLocationData.SplicingPosition#EXON} if the variant is located within an <em>exon</em></li>
 * <li>{@link SplicingLocationData.SplicingPosition#INTRON} if ... you get it</li>
 * </ul>
 * </p>
 */
public class NaiveSplicingTranscriptLocator implements SplicingTranscriptLocator {

    private final SplicingParameters parameters;


    public NaiveSplicingTranscriptLocator(SplicingParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public SplicingLocationData locate(SplicingVariant variant, SplicingTranscript transcript) {
        try {
            // variant and transcript must be present on the same contig
            GenomeCoordinates txCoordinates = transcript.getTxRegionCoordinates();
            if (!variant.getContig().equals(txCoordinates.getContig())) {
                return SplicingLocationData.outside();
            }


            // return outside if variant does not intersect with transcript
            GenomeCoordinates varCoordinates = variant.getCoordinates();
            if (!txCoordinates.overlapsWith(varCoordinates)) {
                return SplicingLocationData.outside();
            }

            final SplicingLocationData.Builder dataBuilder = SplicingLocationData.newBuilder();

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
