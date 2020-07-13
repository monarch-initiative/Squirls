package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.simple;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.MissingFeatureException;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * The first approach for generating SVG graphics. The class applies a set of rules to generate the best SVG graphics
 * for given variant.
 * <p>
 * The generator only works for SNVs.
 */
public class SimpleSplicingVariantGraphicsGenerator implements SplicingVariantGraphicsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSplicingVariantGraphicsGenerator.class);

    /**
     * Field used to ensure that we log a missing feature only once
     */
    private static final AtomicBoolean LOGGED_MISSING_FEATURE = new AtomicBoolean();

    private final VmvtGenerator vmvtGenerator = new VmvtGenerator();

    private final AlleleGenerator alleleGenerator;

    private final SplicingParameters splicingParameters;

    private final SplicingInformationContentCalculator icCalculator;

    private final VisualizationContextSelector selector;

    public SimpleSplicingVariantGraphicsGenerator(SplicingPwmData splicingPwmData) {
        this.alleleGenerator = new AlleleGenerator(splicingPwmData.getParameters());
        this.splicingParameters = splicingPwmData.getParameters();
        this.icCalculator = new SplicingInformationContentCalculator(splicingPwmData);
        this.selector = new VisualizationContextSelector();
    }

    /**
     * @param variant {@link SplicingVariantAlleleEvaluation} to be visualized
     * @return String with SVG image or the default/empty SVG if any required information is missing
     */
    @Override
    public SplicingVariantAlleleEvaluation generateGraphics(SplicingVariantAlleleEvaluation variant) {
        GraphicsData data = GraphicsData.EMPTY;

        if (!variant.getBase().isSNP()) {
            // this class only supports SNVs
            variant.setPrimaryGraphics(data.primary);
            variant.setSecondaryGraphics(data.secondary);
            return variant;
        }

        /*
        Select the prediction data that we use to create the SVG. This is the data that corresponds to transcript with
        respect to which the variant has the maximum predicted pathogenicity.
         */
        final SplicingPredictionData predictionData = variant.getPrimaryPrediction();
        if (predictionData == null) {
            LOGGER.debug("Unable to find transcript with maximum pathogenicity score for variant `{}`",
                    variant.getRepresentation());
            variant.setPrimaryGraphics(data.primary);
            variant.setSecondaryGraphics(data.secondary);
            return variant;
        }

        try {
            final VisualizationContextSelector.VisualizationContext ctx = selector.selectContext(predictionData);
            switch (ctx) {
                case CANONICAL_DONOR:
                    data = makeCanonicalDonorContextGraphics(predictionData);
                    break;
                case CANONICAL_ACCEPTOR:
                    data = makeCanonicalAcceptorContextGraphics(predictionData);
                    break;
                case CRYPTIC_DONOR:
                    data = makeCrypticDonorContextGraphics(predictionData);
                    break;
                case CRYPTIC_ACCEPTOR:
                    data = makeCrypticAcceptorContextGraphics(predictionData);
                    break;
                case SRE:
                    data = makeSreContextGraphics(predictionData);
                    break;
                case UNKNOWN:
                default:
                    break;
            }
        } catch (MissingFeatureException e) {
            if (LOGGED_MISSING_FEATURE.compareAndSet(false, true)) {
                LOGGER.warn("{} : {}", e.getMessage(), variant.getRepresentation());
            }
        }

        variant.setLogo(data.logo);
        variant.setPrimaryGraphics(data.primary);
        variant.setSecondaryGraphics(data.secondary);
        return variant;
    }

    private GraphicsData makeCanonicalDonorContextGraphics(SplicingPredictionData predictionData) {
        final GraphicsData.Builder builder = GraphicsData.builder();

        final GenomeVariant variant = predictionData.getVariant();
        final SequenceInterval sequence = predictionData.getSequence();
        final SplicingTranscript transcript = predictionData.getTranscript();

        // Overlaps with canonical donor site?
        final GenomePosition donorAnchor = predictionData.getMetadata().getDonorCoordinateMap().get(transcript.getAccessionId());

        if (donorAnchor != null) {
            final GenomeInterval canonicalDonorInterval = alleleGenerator.makeDonorInterval(donorAnchor);
            if (variant.getGenomeInterval().overlapsWith(canonicalDonorInterval)) {
                final String refAllele = alleleGenerator.getDonorSiteSnippet(donorAnchor, sequence);
                if (refAllele != null) {
                    final String altAllele = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, variant, sequence);
                    builder.logo(vmvtGenerator.getDonorLogoSvg());
                    builder.primary(vmvtGenerator.getDonorTrekkerSvg(refAllele, altAllele));
                } else {
                    // we cannot set the primary graphics here
                    LOGGER.debug("Unable to get sequence for the canonical donor site `{}` for variant `{}`",
                            canonicalDonorInterval, variant);
                }
            }
        }
        return builder.build();
    }

    private GraphicsData makeCanonicalAcceptorContextGraphics(SplicingPredictionData predictionData) {
        final GraphicsData.Builder builder = GraphicsData.builder();

        final GenomeVariant variant = predictionData.getVariant();
        final SequenceInterval sequence = predictionData.getSequence();
        final SplicingTranscript transcript = predictionData.getTranscript();

        // Overlaps with canonical acceptor site?
        final GenomePosition acceptorAnchor = predictionData.getMetadata().getAcceptorCoordinateMap().get(transcript.getAccessionId());
        if (acceptorAnchor != null) {
            final GenomeInterval canonicalAcceptorInterval = alleleGenerator.makeAcceptorInterval(acceptorAnchor);
            if (variant.getGenomeInterval().overlapsWith(canonicalAcceptorInterval)) {
                final String refAllele = alleleGenerator.getAcceptorSiteSnippet(acceptorAnchor, sequence);
                if (refAllele != null) {
                    final String altAllele = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorAnchor, variant, sequence);
                    builder.logo(vmvtGenerator.getAcceptorLogoSvg());
                    builder.primary(vmvtGenerator.getAcceptorTrekkerSvg(refAllele, altAllele));
                } else {
                    // we cannot set the primary graphics here
                    LOGGER.debug("Unable to get sequence for the canonical acceptor site `{}` for variant `{}`",
                            canonicalAcceptorInterval, variant);
                }
            }
        }
        return builder.build();
    }

    private GraphicsData makeCrypticDonorContextGraphics(SplicingPredictionData predictionData) {
        /*
         * Find the position of the best ALT window site
         */
        final GraphicsData.Builder builder = GraphicsData.builder();

        final SequenceInterval sequence = predictionData.getSequence();
        final GenomeVariant variant = predictionData.getVariant();
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        final String refSnippet = alleleGenerator.getDonorNeighborSnippet(variantInterval, sequence, variant.getRef());
        final String altSnippet = alleleGenerator.getDonorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (refSnippet == null || altSnippet == null) {
            // nothing more to be done
            return builder.build();
        }

        final List<Double> altDonorScores = Utils.slidingWindow(altSnippet, splicingParameters.getDonorLength())
                .map(icCalculator::getSpliceDonorScore)
                .collect(Collectors.toList());

        final int altMaxIdx = Utils.argmax(altDonorScores);

        // primary - trekker comparing the best ALT window with the corresponding REF window
        final String altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
        final String refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
        builder.primary(vmvtGenerator.getDonorTrekkerSvg(refCorrespondingWindow, altBestWindow));

        // secondary - sequence walkers comparing the best ALT window with the canonical donor snippet
        final GenomePosition donorAnchor = predictionData.getMetadata().getDonorCoordinateMap().get(predictionData.getTranscript().getAccessionId());
        final String canonicalDonorSnippet = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, variant, sequence);
        builder.secondary(vmvtGenerator.getDonorCanonicalCryptic(canonicalDonorSnippet, altBestWindow));

        return builder.build();
    }

    private GraphicsData makeCrypticAcceptorContextGraphics(SplicingPredictionData predictionData) {
        /*
         * Find the position of the best ALT window site
         */
        final GraphicsData.Builder builder = GraphicsData.builder();

        final SequenceInterval sequence = predictionData.getSequence();
        final GenomeVariant variant = predictionData.getVariant();
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        final String refSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getRef());
        final String altSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (refSnippet == null || altSnippet == null) {
            // nothing more to be done
            return builder.build();
        }

        final List<Double> altAcceptorScores = Utils.slidingWindow(altSnippet, splicingParameters.getAcceptorLength())
                .map(icCalculator::getSpliceAcceptorScore)
                .collect(Collectors.toList());

        final int altMaxIdx = Utils.argmax(altAcceptorScores);

        // primary - trekker comparing the best ALT window with the corresponding REF window
        final String altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
        final String refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
        builder.primary(vmvtGenerator.getAcceptorTrekkerSvg(refCorrespondingWindow, altBestWindow));

        // secondary - sequence walkers comparing the best ALT window with the canonical acceptor snippet
        final GenomePosition acceptorAnchor = predictionData.getMetadata().getAcceptorCoordinateMap().get(predictionData.getTranscript().getAccessionId());
        final String canonicalAcceptorSnippet = alleleGenerator.getDonorSiteWithAltAllele(acceptorAnchor, variant, sequence);
        builder.secondary(vmvtGenerator.getAcceptorCanonicalCryptic(canonicalAcceptorSnippet, altBestWindow));

        return builder.build();
    }

    private GraphicsData makeSreContextGraphics(SplicingPredictionData predictionData) {
        final GraphicsData.Builder builder = GraphicsData.builder();
        final GenomeVariant variant = predictionData.getVariant();
        final SequenceInterval sequence = predictionData.getSequence();

        // primary - hexamer
        final String hexamerRefSnippet = alleleGenerator.getKmerRefSnippet(variant, sequence, 6);  // hexamer = 6
        final String hexamerAltSnippet = alleleGenerator.getKmerAltSnippet(variant, sequence, 6);
        if (hexamerRefSnippet != null && hexamerAltSnippet != null) {
            builder.primary(vmvtGenerator.getHexamerSvg(hexamerRefSnippet, hexamerAltSnippet));
        }

        // secondary - heptamer
        final String heptamerRefSnippet = alleleGenerator.getKmerRefSnippet(variant, sequence, 7); // heptamer = 7
        final String heptamerAltSnippet = alleleGenerator.getKmerAltSnippet(variant, sequence, 7);
        if (heptamerRefSnippet != null && heptamerAltSnippet != null) {
            builder.secondary(vmvtGenerator.getHeptamerSvg(heptamerRefSnippet, heptamerAltSnippet));
        }

        return builder.build();
    }

    private static class GraphicsData {

        /**
         * The image returned if unable to generate the normal SVG.
         */
        private static final String EMPTY_SVG_IMAGE = "<svg width=\"20\" height=\"20\" style=\"border:1px solid black\" xmlns=\"http://www.w3.org/2000/svg\"></svg>";

        private static final GraphicsData EMPTY = GraphicsData.builder().build();

        private final String logo;
        private final String primary;
        private final String secondary;

        private GraphicsData(Builder builder) {
            logo = builder.logo;
            primary = builder.primary;
            secondary = builder.secondary;
        }

        public static Builder builder() {
            return new Builder();
        }

        private static final class Builder {
            private String logo = EMPTY_SVG_IMAGE;
            private String primary = EMPTY_SVG_IMAGE;
            private String secondary = EMPTY_SVG_IMAGE;

            private Builder() {
            }

            private Builder logo(String logo) {
                this.logo = logo;
                return this;
            }

            private Builder primary(String primary) {
                this.primary = primary;
                return this;
            }

            private Builder secondary(String secondary) {
                this.secondary = secondary;
                return this;
            }

            private GraphicsData build() {
                return new GraphicsData(this);
            }
        }
    }
}
