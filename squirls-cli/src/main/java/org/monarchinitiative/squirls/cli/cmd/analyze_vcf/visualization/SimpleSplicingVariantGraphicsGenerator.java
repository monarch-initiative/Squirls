package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.vmvt.VmvtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * The first approach for generating SVG graphics. The class applies a set of rules to generate the best SVG graphics
 * for given variant.
 */
public class SimpleSplicingVariantGraphicsGenerator implements SplicingVariantGraphicsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSplicingVariantGraphicsGenerator.class);

    // TODO: 29. 6. 2020 revise if necessary
    /**
     * The image returned if unable to generate the normal SVG.
     */
    private static final String EMPTY_SVG_IMAGE = "<svg width=\"20\" height=\"20\" xmlns=\"http://www.w3.org/2000/svg\"></svg>";

    private final VmvtGenerator vmvtGenerator = new VmvtGenerator();

    private final AlleleGenerator alleleGenerator;

    public SimpleSplicingVariantGraphicsGenerator(AlleleGenerator alleleGenerator) {
        this.alleleGenerator = alleleGenerator;
    }

    /**
     * @param variant {@link SplicingVariantAlleleEvaluation} to be visualized
     * @return String with SVG image or the default/empty SVG if any required information is missing
     */
    @Override
    public String generateGraphics(SplicingVariantAlleleEvaluation variant) {
        /*
        Select the prediction data that we use to create the SVG. This is the data that corresponds to transcript with
        respect to which the variant has the maximum predicted pathogenicity.
         */
        final Optional<SplicingPredictionData> optPredictionData = variant.getPredictionData().entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue().getPrediction().getMaxPathogenicity()))
                .map(Map.Entry::getValue);
        if (optPredictionData.isEmpty()) {
            LOGGER.debug("Unable to find transcript with maximum pathogenicity score for variant `{}`",
                    variant.getRepresentation());
            return EMPTY_SVG_IMAGE;
        }

        /*
        This is the set of data we shall use for generation of the SVG graphics.
         */
        final SplicingPredictionData predictionData = optPredictionData.get();
        final SplicingTranscript transcript = predictionData.getTranscript();
        final GenomeVariant genomeVariant = predictionData.getVariant();
        final SequenceInterval sequence = predictionData.getSequence();

        /*
        Overlaps with canonical donor site?
         */
        final GenomePosition donorAnchor = predictionData.getMetadata().getDonorCoordinateMap().get(transcript.getAccessionId());
        if (donorAnchor != null) {
            final GenomeInterval canonicalDonorInterval = alleleGenerator.makeDonorInterval(donorAnchor);
            if (genomeVariant.getGenomeInterval().overlapsWith(canonicalDonorInterval)) {
                final String refAllele = alleleGenerator.getDonorSiteSnippet(donorAnchor, sequence);
                if (refAllele == null) {
                    LOGGER.debug("Unable to get sequence for the canonical donor site `{}` for variant `{}`",
                            canonicalDonorInterval, variant.getRepresentation());
                    return EMPTY_SVG_IMAGE;
                }

                final String altAllele = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, genomeVariant, sequence);
                return vmvtGenerator.getDonorVmvtSvg(refAllele, altAllele);
            }
        }

        /*
        Overlaps with canonical acceptor site?
         */
        final GenomePosition acceptorAnchor = predictionData.getMetadata().getAcceptorCoordinateMap().get(transcript.getAccessionId());
        if (acceptorAnchor != null) {
            final GenomeInterval canonicalAcceptorInterval = alleleGenerator.makeAcceptorInterval(acceptorAnchor);
            if (genomeVariant.getGenomeInterval().overlapsWith(canonicalAcceptorInterval)) {
                final String refAllele = alleleGenerator.getAcceptorSiteSnippet(acceptorAnchor, sequence);
                if (refAllele == null) {
                    LOGGER.debug("Unable to get sequence for the canonical acceptor site `{}` for variant `{}`",
                            canonicalAcceptorInterval, variant.getRepresentation());
                    return EMPTY_SVG_IMAGE;
                }

                final String altAllele = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorAnchor, genomeVariant, sequence);
                return vmvtGenerator.getAcceptorVmvtSvg(refAllele, altAllele);
            }
        }


        /*
         Is the variant in exon? Then we show
         -
         */

        // TODO: 29. 6. 2020 implement!

        return "";
    }
}
