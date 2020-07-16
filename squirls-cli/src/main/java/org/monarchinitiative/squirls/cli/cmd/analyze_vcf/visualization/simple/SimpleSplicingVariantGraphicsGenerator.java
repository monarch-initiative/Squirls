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
     * The image returned if unable to generate the normal SVG.
     */
    private static final String EMPTY_SVG_IMAGE = "<svg width=\"100\" height=\"5\" xmlns=\"http://www.w3.org/2000/svg\"></svg>";

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
     * Put together the figures into a single titled <code>div</code> element.
     *
     * @param context visualization context for which the figures were created
     * @param figures SVG strings, <code>null</code>s are ignored
     * @return string with titled div containing all figures
     */
    private static String assembleFigures(VisualizationContext context, String... figures) {
        final StringBuilder graphics = new StringBuilder();
        // add title
        graphics.append("<div class=\"graphics-container\">")
                .append("<div class=\"graphics-title\">").append(context.getTitle()).append("</div>")
                .append("<div class=\"graphics-content\">");
        // add figures
        for (String figure : figures) {
            if (figure != null) {
                graphics.append("<div>").append(figure).append("</div>");
            }
        }

        // close tags
        return graphics.append("</div>") // graphics-content
                .append("</div>") // graphics-container
                .toString();
    }

    /**
     * @param variant {@link SplicingVariantAlleleEvaluation} to be visualized
     * @return String with SVG image or the default/empty SVG if any required information is missing
     */
    @Override
    public String generateGraphics(SplicingVariantAlleleEvaluation variant) {
        if (!variant.getBase().isSNP()) {
            // this class only supports SNVs
            return EMPTY_SVG_IMAGE;
        }

        /*
        Select the prediction data that we use to create the SVG. This is the data that corresponds to transcript with
        respect to which the variant has the maximum predicted pathogenicity.
         */
        final SplicingPredictionData predictionData = variant.getPrimaryPrediction();
        if (predictionData == null) {
            LOGGER.debug("Unable to find transcript with maximum pathogenicity score for variant `{}`",
                    variant.getRepresentation());
            return EMPTY_SVG_IMAGE;
        }

        try {
            final VisualizationContext ctx = selector.selectContext(predictionData);
            switch (ctx) {
                case CANONICAL_DONOR:
                    return makeCanonicalDonorContextGraphics(predictionData);
                case CANONICAL_ACCEPTOR:
                    return makeCanonicalAcceptorContextGraphics(predictionData);
                case CRYPTIC_DONOR:
                    return makeCrypticDonorContextGraphics(predictionData);
                case CRYPTIC_ACCEPTOR:
                    return makeCrypticAcceptorContextGraphics(predictionData);
                case SRE:
                    return makeSreContextGraphics(predictionData);
                case UNKNOWN:
                default:
            }
        } catch (MissingFeatureException e) {
            if (LOGGED_MISSING_FEATURE.compareAndSet(false, true)) {
                LOGGER.warn("{} : {}", e.getMessage(), variant.getRepresentation());
            }
        }

        return EMPTY_SVG_IMAGE;
    }

    private String makeCanonicalDonorContextGraphics(SplicingPredictionData predictionData) {
        final VisualizationContext context = VisualizationContext.CANONICAL_DONOR;

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
                    return assembleFigures(context,
                            vmvtGenerator.getDonorSequenceRuler(refAllele, altAllele),
                            vmvtGenerator.getDonorTrekkerSvg(refAllele, altAllele),
                            vmvtGenerator.getDonorDistributionSvg(refAllele, altAllele));
                } else {
                    // we cannot set the primary graphics here
                    LOGGER.debug("Unable to get sequence for the canonical donor site `{}` for variant `{}`",
                            canonicalDonorInterval, variant);
                }
            }
        }
        return EMPTY_SVG_IMAGE;
    }

    private String makeCanonicalAcceptorContextGraphics(SplicingPredictionData predictionData) {
        final VisualizationContext context = VisualizationContext.CANONICAL_ACCEPTOR;
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
                    return assembleFigures(context,
                            vmvtGenerator.getAcceptorSequenceRuler(refAllele, altAllele),
                            vmvtGenerator.getAcceptorTrekkerSvg(refAllele, altAllele),
                            vmvtGenerator.getAcceptorDistributionSvg(refAllele, altAllele));
                } else {
                    // we cannot set the primary graphics here
                    LOGGER.debug("Unable to get sequence for the canonical acceptor site `{}` for variant `{}`",
                            canonicalAcceptorInterval, variant);
                }
            }
        }
        return EMPTY_SVG_IMAGE;
    }

    private String makeCrypticDonorContextGraphics(SplicingPredictionData predictionData) {
        final VisualizationContext context = VisualizationContext.CRYPTIC_DONOR;

        final SequenceInterval sequence = predictionData.getSequence();
        final GenomeVariant variant = predictionData.getVariant();
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        final String refSnippet = alleleGenerator.getDonorNeighborSnippet(variantInterval, sequence, variant.getRef());
        final String altSnippet = alleleGenerator.getDonorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (refSnippet == null || altSnippet == null) {
            // nothing more to be done
            return EMPTY_SVG_IMAGE;
        }

        final List<Double> altDonorScores = Utils.slidingWindow(altSnippet, splicingParameters.getDonorLength())
                .map(icCalculator::getSpliceDonorScore)
                .collect(Collectors.toList());

        final int altMaxIdx = Utils.argmax(altDonorScores);

        // primary - trekker comparing the best ALT window with the corresponding REF window
        final String altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
        final String refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
        final String trekker = vmvtGenerator.getDonorTrekkerSvg(refCorrespondingWindow, altBestWindow);

        // secondary - sequence walkers comparing the best ALT window with the canonical donor snippet
        final GenomePosition donorAnchor = predictionData.getMetadata().getDonorCoordinateMap().get(predictionData.getTranscript().getAccessionId());
        final String walkers;
        if (donorAnchor != null) {
            // we have the anchor, thus let's make the graphics
            final String canonicalDonorSnippet = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, variant, sequence);
            walkers = vmvtGenerator.getDonorCanonicalCryptic(canonicalDonorSnippet, altBestWindow);
        } else {
            // there is no anchor, this happens in single-exon transcripts
            if (!predictionData.getTranscript().getIntrons().isEmpty()) {
                // however, complain if this is not single-exon transcript!
                LOGGER.warn("Did not find donor site in metadata while but the transcript has {} intron(s)",
                        predictionData.getTranscript().getIntrons().size());
            }
            walkers = null;
        }

        return assembleFigures(context, trekker, walkers);
    }

    private String makeCrypticAcceptorContextGraphics(SplicingPredictionData predictionData) {
        final VisualizationContext context = VisualizationContext.CRYPTIC_DONOR;

        final SequenceInterval sequence = predictionData.getSequence();
        final GenomeVariant variant = predictionData.getVariant();
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        final String refSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getRef());
        final String altSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (refSnippet == null || altSnippet == null) {
            // nothing more to be done
            return EMPTY_SVG_IMAGE;
        }

        final List<Double> altAcceptorScores = Utils.slidingWindow(altSnippet, splicingParameters.getAcceptorLength())
                .map(icCalculator::getSpliceAcceptorScore)
                .collect(Collectors.toList());

        final int altMaxIdx = Utils.argmax(altAcceptorScores);

        // primary - trekker comparing the best ALT window with the corresponding REF window
        final String altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
        final String refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
        final String trekker = vmvtGenerator.getAcceptorTrekkerSvg(refCorrespondingWindow, altBestWindow);

        // secondary - sequence walkers comparing the best ALT window with the canonical acceptor snippet
        final GenomePosition acceptorAnchor = predictionData.getMetadata().getAcceptorCoordinateMap().get(predictionData.getTranscript().getAccessionId());
        final String walkers;
        if (acceptorAnchor != null) {
            // we have the anchor, thus let's make the graphics
            final String canonicalAcceptorSnippet = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorAnchor, variant, sequence);
            walkers = vmvtGenerator.getAcceptorCanonicalCryptic(canonicalAcceptorSnippet, altBestWindow);
        } else {
            // there is no anchor, this happens in single-exon transcripts
            if (!predictionData.getTranscript().getIntrons().isEmpty()) {
                // however, complain if this is not single-exon transcript!
                LOGGER.warn("Did not find acceptor site in metadata while but the transcript has {} intron(s)",
                        predictionData.getTranscript().getIntrons().size());
            }
            walkers = null;
        }

        return assembleFigures(context, trekker, walkers);
    }

    private String makeSreContextGraphics(SplicingPredictionData predictionData) {
        final VisualizationContext context = VisualizationContext.SRE;

        final GenomeVariant variant = predictionData.getVariant();
        final SequenceInterval sequence = predictionData.getSequence();

        // primary - hexamer
        final String hexamerRefSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 5);  // 5 because at least one base is REF/ALT
        final String hexamerAltSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 5);
        final String hexamerGraphics = hexamerRefSnippet != null && hexamerAltSnippet != null
                ? vmvtGenerator.getHexamerSvg(hexamerRefSnippet, hexamerAltSnippet)
                : null;

        // secondary - heptamer
        final String heptamerRefSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 6); // 6 because at least one base is REF/ALT
        final String heptamerAltSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 6);
        final String heptamerGraphics = heptamerRefSnippet != null && heptamerAltSnippet != null
                ? vmvtGenerator.getHeptamerSvg(heptamerRefSnippet, heptamerAltSnippet)
                : null;

        // at least one figure is present
        if (hexamerGraphics != null || heptamerGraphics != null) {
            return assembleFigures(context, hexamerGraphics, heptamerGraphics);
        }
        return EMPTY_SVG_IMAGE;
    }
}
