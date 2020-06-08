package org.monarchinitiative.threes.core.reference.transcript;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
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

        int n_introns = transcript.getIntrons().size();
        int n_exons = transcript.getExons().size();
        assert n_introns == n_exons - 1;
        final SplicingLocationData.Builder locationData = SplicingLocationData.builder();

        // is this a single exon gene?
        if (n_exons == 1) {
            // nothing more to be solved, variant intersects with transcript as checked above.
            // SplicingPosition must be EXON
            return locationData
                    .setExonIndex(0)
                    .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                    .build();
        }

        // we iterate through INTRONS
        for (int i = 0; i < n_introns; i++) {
            final SplicingExon exon = transcript.getExons().get(i);
            final SplicingIntron intron = transcript.getIntrons().get(i);

            final GenomeInterval donor = parameters.makeDonorRegion(intron.getInterval().getGenomeBeginPos());
            final GenomeInterval acceptor = parameters.makeAcceptorRegion(intron.getInterval().getGenomeEndPos());

            // 1 - does the variant overlap with the donor site?
            final GenomePosition exonBeginPosition = exon.getInterval().getGenomeBeginPos();
            if (donor.overlapsWith(variantInterval)) {
                if (i != 0) {
                    // this is not the first exon, so set the acceptor site of this exon as the acceptor location
                    locationData.setAcceptorBoundary(exonBeginPosition)
                            .setAcceptorRegion(parameters.makeAcceptorRegion(exonBeginPosition));
                }
                return locationData.setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                        .setDonorBoundary(intron.getInterval().getGenomeBeginPos())
                        .setDonorRegion(donor)
                        .setIntronIndex(i)
                        .setExonIndex(i)
                        .build();
            }

            // 2 - does the variant overlap with the acceptor site?
            if (acceptor.overlapsWith(variantInterval)) {
                locationData.setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                        .setAcceptorBoundary(intron.getInterval().getGenomeEndPos())
                        .setAcceptorRegion(acceptor)
                        .setIntronIndex(i)
                        .setExonIndex(i + 1);
                if (i != n_introns - 1) {
                    // we are not processing the last intron. If this is not the acceptor site of the last intron,
                    // then we have a splice donor site
                    final GenomePosition donorBoundary = transcript.getExons().get(i + 1).getInterval().getGenomeEndPos();
                    locationData.setDonorBoundary(donorBoundary)
                            .setDonorRegion(parameters.makeDonorRegion(donorBoundary));
                }
                // We iterate through introns so the current `donor` defined above is the donor of the previous exon.
                // Let's use the donor of the current exon!
                return locationData.build();
            }

            // 3 - does the variant overlap with the current intron?
            if (intron.getInterval().overlapsWith(variantInterval)) {
                return locationData
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.INTRON)
                        .setDonorBoundary(intron.getInterval().getGenomeBeginPos())
                        .setDonorRegion(donor)
                        .setAcceptorBoundary(intron.getInterval().getGenomeEndPos())
                        .setAcceptorRegion(acceptor)
                        .setIntronIndex(i)
                        .build();
            }

            // 4 - does the variant overlap with the current exon?
            if (exon.getInterval().overlapsWith(variantInterval)) {
                final GenomePosition donorBoundary = exon.getInterval().getGenomeEndPos();
                locationData
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                        .setDonorBoundary(donorBoundary)
                        .setDonorRegion(parameters.makeDonorRegion(donorBoundary))
                        .setExonIndex(i);
                if (i != 0) {
                    // we're not processing the first exon, so we have the acceptor site
                    locationData.setAcceptorBoundary(exonBeginPosition)
                            .setAcceptorRegion(parameters.makeAcceptorRegion(exonBeginPosition));
                }
                return locationData.build();
            }
        }

        // For transcript with x exons we processed x-1 exons and x introns above. There is no overlap with previous
        // exons/introns/canonical splice sites if we get here. Since the variant overlaps with the transcript, it
        // has to overlap with the last exon.

        // the last exon does not have the donor site, hence not setting the donor boundary
        final int lastExonIdx = n_exons - 1;
        final GenomePosition acceptorBoundary = transcript.getExons().get(lastExonIdx).getInterval().getGenomeBeginPos();
        return locationData
                .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                .setExonIndex(lastExonIdx)
                .setAcceptorBoundary(acceptorBoundary)
                .setAcceptorRegion(parameters.makeAcceptorRegion(acceptorBoundary))
                .build();
    }
}
