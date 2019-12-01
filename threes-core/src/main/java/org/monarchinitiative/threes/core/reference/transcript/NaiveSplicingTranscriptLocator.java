package org.monarchinitiative.threes.core.reference.transcript;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;

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
    public SplicingLocationData locate(GenomeVariant variant, SplicingTranscript transcript) {
        // variant and transcript must be present on the same contig
        GenomeInterval txCoordinates = transcript.getTxRegionCoordinates();
        if (variant.getChr() != txCoordinates.getChr()) {
            return SplicingLocationData.outside();
        }


        // return outside if variant does not intersect with transcript
        GenomeInterval variantInterval = variant.getGenomeInterval();
        if (!txCoordinates.overlapsWith(variantInterval)) {
            return SplicingLocationData.outside();
        }

        final SplicingLocationData.Builder dataBuilder = SplicingLocationData.builder();

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
            final GenomeInterval donor = parameters.makeDonorRegion(intron.getInterval().getGenomeBeginPos());

            if (donor.overlapsWith(variantInterval)) {
                if (i != 0) {
                    // first exon does not have acceptor site
                    dataBuilder.setAcceptorBoundary(exon.getInterval().getGenomeBeginPos());
                }
                return dataBuilder.setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                        .setDonorBoundary(intron.getInterval().getGenomeBeginPos())
                        .setIntronIndex(i)
                        .setExonIndex(i)
                        .build();
            }

            // 2 - is the variant in the acceptor site?
            // acceptor
            final GenomeInterval acceptor = parameters.makeAcceptorRegion(intron.getInterval().getGenomeEndPos());

            if (acceptor.overlapsWith(variantInterval)) {
                return dataBuilder
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                        .setDonorBoundary(transcript.getExons().get(i + 1).getInterval().getGenomeEndPos())
                        .setAcceptorBoundary(intron.getInterval().getGenomeEndPos())
                        .setIntronIndex(i)
                        .setExonIndex(i + 1)
                        .build();
            }

            // 3 - does the variant overlap with the current intron?
            if (intron.getInterval().overlapsWith(variantInterval)) {
                return dataBuilder
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.INTRON)
                        .setDonorBoundary(intron.getInterval().getGenomeBeginPos())
                        .setAcceptorBoundary(intron.getInterval().getGenomeEndPos())
                        .setIntronIndex(i)
                        .build();
            }

            // 4 - does the variant overlap with the current exon?
            if (exon.getInterval().overlapsWith(variantInterval)) {
                return dataBuilder
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                        .setDonorBoundary(exon.getInterval().getGenomeEndPos())
                        .setAcceptorBoundary(exon.getInterval().getGenomeBeginPos())
                        .setExonIndex(i)
                        .build();
            }
        }

        // For transcript with x exons we processed x-1 exons and x introns above. There is no overlap with previous
        // exons/introns/canonical splice sites if we get here. Since the variant overlaps with the transcript, it
        // has to overlap with the last exon.

        // the last exon does not have the donor site, hence not setting the donor boundary
        final int lastExonIdx = transcript.getExons().size() - 1;
        return dataBuilder
                .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                .setExonIndex(lastExonIdx)
                .setAcceptorBoundary(transcript.getExons().get(lastExonIdx).getInterval().getGenomeBeginPos())
                .build();
    }
}
