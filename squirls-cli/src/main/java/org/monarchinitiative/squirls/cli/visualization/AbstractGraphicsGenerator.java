package org.monarchinitiative.squirls.cli.visualization;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContextSelector;
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
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractGraphicsGenerator implements SplicingVariantGraphicsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphicsGenerator.class);

    protected final VmvtGenerator vmvtGenerator;

    protected final VisualizationContextSelector contextSelector;

    private final AlleleGenerator alleleGenerator;

    private final SplicingParameters splicingParameters;

    private final SplicingInformationContentCalculator icCalculator;

    private final GenomeSequenceAccessor genomeSequenceAccessor;

    protected AbstractGraphicsGenerator(VmvtGenerator vmvtGenerator,
                                        SplicingPwmData splicingPwmData,
                                        VisualizationContextSelector contextSelector,
                                        GenomeSequenceAccessor genomeSequenceAccessor) {

        this.vmvtGenerator = vmvtGenerator;
        this.splicingParameters = splicingPwmData.getParameters();
        this.alleleGenerator = new AlleleGenerator(splicingPwmData.getParameters());
        this.icCalculator = new SplicingInformationContentCalculator(splicingPwmData);
        this.contextSelector = contextSelector;
        this.genomeSequenceAccessor = genomeSequenceAccessor;
    }

    /**
     * Assemble SVG & HTML code into a single HTML string consisting of title and SVG graphics.
     *
     * @param title     title label
     * @param primary   SVG string with primary graphics
     * @param secondary SVG string with secondary graphics
     * @return result HTML string
     */
    private static String makeCrypticContextGraphics(String title, String primary, String secondary) {
        StringBuilder graphics = new StringBuilder();
        // add title
        graphics.append("<div class=\"graphics-container\">")
                .append("<div class=\"graphics-title\">").append(title).append("</div>")
                .append("<div class=\"graphics-content\">");

        // primary graphics
        graphics.append("<div class=\"graphics-subcontent\">")
                .append(primary)
                .append("</div>");

        // secondary graphics
        graphics.append("<div class=\"graphics-subcontent\">")
                .append(secondary)
                .append("</div>");

        // close tags
        return graphics.append("</div>") // graphics-content
                .append("</div>") // graphics-container
                .toString();
    }

    /**
     * Put together the figures into a single titled <code>div</code> element.
     *
     * @param context visualization context for which the figures were created
     * @param figures SVG strings, <code>null</code>s are ignored
     * @return string with titled div containing all figures
     */
    private static String assembleFigures(VisualizationContext context, String... figures) {
        StringBuilder graphics = new StringBuilder();
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
     * Generate graphics for variants affecting canonical donor.
     * <p>
     * We show:
     * <ol>
     *     <li>primary graphics:
     *     <ul>
     *         <li>sequence ruler</li>
     *         <li>sequence trekker</li>
     *     </ul>
     *     </li>
     *     <li>secondary graphics:
     *     <ul>
     *          <li>position of variant delta Ri in the Ri distribution</li>
     *     </ul>
     *     </li>
     * </ol>
     *
     * @param predictionData data to generate the graphics for
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCanonicalDonorContextGraphics(SplicingPredictionData predictionData) {
        VisualizationContext context = VisualizationContext.CANONICAL_DONOR;

        GenomeVariant variant = predictionData.getVariant();
        SplicingTranscript transcript = predictionData.getTranscript();
        Optional<SequenceInterval> sio = fetchSequenceForTranscript(transcript);
        if (sio.isEmpty()) {
            return EMPTY_SVG_IMAGE;
        }
        SequenceInterval sequence = sio.get();

        // Overlaps with canonical donor site?
        GenomePosition donorAnchor = predictionData.getMetadata().getDonorCoordinateMap().get(transcript.getAccessionId());

        if (donorAnchor != null) {
            GenomeInterval canonicalDonorInterval = alleleGenerator.makeDonorInterval(donorAnchor);
            if (variant.getGenomeInterval().overlapsWith(canonicalDonorInterval)) {
                String refAllele = alleleGenerator.getDonorSiteSnippet(donorAnchor, sequence);
                if (refAllele != null) {
                    String altAllele = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, variant, sequence);

                    StringBuilder graphics = new StringBuilder();
                    // add title
                    graphics.append("<div class=\"graphics-container\">")
                            .append("<div class=\"graphics-title\">").append(context.getTitle()).append("</div>")
                            .append("<div class=\"graphics-content\">");

                    // primary - add ruler and trekker
                    graphics.append("<div class=\"graphics-subcontent\">")
                            .append(vmvtGenerator.getDonorSequenceRuler(refAllele, altAllele))
                            .append(vmvtGenerator.getDonorTrekkerSvg(refAllele, altAllele))
//                            .append(vmvtGenerator.getDonorWithRi(refAllele, altAllele))
                            .append("</div>");

                    // secondary - add distribution
                    graphics.append("<div class=\"graphics-subcontent\">")
                            .append(vmvtGenerator.getDonorDistributionSvg(refAllele, altAllele))
                            .append("</div>");

                    // close tags
                    return graphics.append("</div>") // graphics-content
                            .append("</div>") // graphics-container
                            .toString();

                } else {
                    // we cannot set the primary graphics here
                    LOGGER.debug("Unable to get sequence for the canonical donor site `{}` for variant `{}`",
                            canonicalDonorInterval, variant);
                }
            }
        }
        return EMPTY_SVG_IMAGE;
    }

    /**
     * Generate graphics for variants affecting canonical acceptor.
     * <p>
     * We show:
     * <ol>
     *     <li>primary graphics:
     *     <ul>
     *         <li>sequence ruler</li>
     *         <li>sequence trekker</li>
     *     </ul>
     *     </li>
     *     <li>secondary graphics:
     *     <ul>
     *          <li>position of variant delta Ri in the Ri distribution</li>
     *     </ul>
     *     </li>
     * </ol>
     *
     * @param predictionData data to generate the graphics for
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCanonicalAcceptorContextGraphics(SplicingPredictionData predictionData) {
        VisualizationContext context = VisualizationContext.CANONICAL_ACCEPTOR;
        GenomeVariant variant = predictionData.getVariant();
        SplicingTranscript transcript = predictionData.getTranscript();
        Optional<SequenceInterval> sio = fetchSequenceForTranscript(transcript);
        if (sio.isEmpty()) {
            return EMPTY_SVG_IMAGE;
        }
        SequenceInterval sequence = sio.get();

        // Overlaps with canonical acceptor site?
        GenomePosition acceptorAnchor = predictionData.getMetadata().getAcceptorCoordinateMap().get(transcript.getAccessionId());
        if (acceptorAnchor != null) {
            GenomeInterval canonicalAcceptorInterval = alleleGenerator.makeAcceptorInterval(acceptorAnchor);
            if (variant.getGenomeInterval().overlapsWith(canonicalAcceptorInterval)) {
                String refAllele = alleleGenerator.getAcceptorSiteSnippet(acceptorAnchor, sequence);
                if (refAllele != null) {
                    String altAllele = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorAnchor, variant, sequence);

                    StringBuilder graphics = new StringBuilder();
                    // add title
                    graphics.append("<div class=\"graphics-container\">")
                            .append("<div class=\"graphics-title\">").append(context.getTitle()).append("</div>")
                            .append("<div class=\"graphics-content\">");

                    // primary - add ruler and trekker
                    graphics.append("<div class=\"graphics-subcontent\">")
                            .append(vmvtGenerator.getAcceptorSequenceRuler(refAllele, altAllele))
                            .append(vmvtGenerator.getAcceptorTrekkerSvg(refAllele, altAllele))
//                            .append(vmvtGenerator.getAcceptorWithRi(refAllele, altAllele))
                            .append("</div>");

                    // secondary - add distribution
                    graphics.append("<div class=\"graphics-subcontent\">")
                            .append(vmvtGenerator.getAcceptorDistributionSvg(refAllele, altAllele))
                            .append("</div>");

                    // close tags
                    return graphics.append("</div>") // graphics-content
                            .append("</div>") // graphics-container
                            .toString();
                } else {
                    // we cannot set the primary graphics here
                    LOGGER.debug("Unable to get sequence for the canonical acceptor site `{}` for variant `{}`",
                            canonicalAcceptorInterval, variant);
                }
            }
        }
        return EMPTY_SVG_IMAGE;
    }

    /**
     * Generate graphics for variants leading to creation of a cryptic donor site.
     * <p>
     * We show:
     * <ol>
     *     <li>primary graphics:
     *     <ul>
     *         <li>sequence ruler</li>
     *         <li>sequence trekker</li>
     *     </ul>
     *     </li>
     *     <li>secondary graphics:
     *     <ul>
     *         <li>sequence walker of canonical alt sequence vs. sequence walker of cryptic alt sequence</li>
     *     </ul>
     *     </li>
     * </ol>
     *
     * @param predictionData data to generate the graphics for
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCrypticDonorContextGraphics(SplicingPredictionData predictionData) {
        VisualizationContext context = VisualizationContext.CRYPTIC_DONOR;

        GenomeVariant variant = predictionData.getVariant();
        GenomeInterval variantInterval = variant.getGenomeInterval();
        Optional<SequenceInterval> sio = fetchSequenceForTranscript(predictionData.getTranscript());
        if (sio.isEmpty()) {
            return EMPTY_SVG_IMAGE;
        }
        SequenceInterval sequence = sio.get();

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        String refSnippet = alleleGenerator.getDonorNeighborSnippet(variantInterval, sequence, variant.getRef());
        String altSnippet = alleleGenerator.getDonorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (refSnippet == null || altSnippet == null) {
            // nothing more to be done
            return EMPTY_SVG_IMAGE;
        }

        List<Double> altDonorScores = Utils.slidingWindow(altSnippet, splicingParameters.getDonorLength())
                .map(icCalculator::getSpliceDonorScore)
                .collect(Collectors.toList());

        int altMaxIdx = Utils.argmax(altDonorScores);

        // primary - trekker comparing the best ALT window with the corresponding REF window
        String altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
        String refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
        String trekker = vmvtGenerator.getDonorTrekkerSvg(refCorrespondingWindow, altBestWindow);

        // secondary - sequence walkers comparing the best ALT window with the canonical donor snippet
        GenomePosition donorAnchor = predictionData.getMetadata().getDonorCoordinateMap().get(predictionData.getTranscript().getAccessionId());
        String walkers;
        if (donorAnchor != null) {
            // we have the anchor, thus let's make the graphics
            String canonicalDonorSnippet = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, variant, sequence);
            walkers = vmvtGenerator.getDonorCanonicalCryptic(canonicalDonorSnippet, altBestWindow);
        } else {
            // there is no anchor, this happens in single-exon transcripts
            if (!predictionData.getTranscript().getIntrons().isEmpty()) {
                // however, complain if this is not a single-exon transcript!
                LOGGER.warn("Did not find donor site in metadata while but the transcript has {} intron(s)",
                        predictionData.getTranscript().getIntrons().size());
            }
            walkers = EMPTY_SVG_IMAGE;
        }

        return makeCrypticContextGraphics(context.getTitle(), trekker, walkers);
    }

    /**
     * Generate graphics for variants leading to creation of a cryptic acceptor site.
     * <p>
     * We show:
     * <ol>
     *     <li>primary graphics:
     *     <ul>
     *         <li>sequence ruler</li>
     *         <li>sequence trekker</li>
     *     </ul>
     *     </li>
     *     <li>secondary graphics:
     *     <ul>
     *         <li>sequence walker of canonical alt sequence vs. sequence walker of cryptic alt sequence</li>
     *     </ul>
     *     </li>
     * </ol>
     *
     * @param predictionData data to generate the graphics for
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCrypticAcceptorContextGraphics(SplicingPredictionData predictionData) {
        VisualizationContext context = VisualizationContext.CRYPTIC_ACCEPTOR;

        GenomeVariant variant = predictionData.getVariant();
        GenomeInterval variantInterval = variant.getGenomeInterval();
        Optional<SequenceInterval> sio = fetchSequenceForTranscript(predictionData.getTranscript());
        if (sio.isEmpty()) {
            return EMPTY_SVG_IMAGE;
        }
        SequenceInterval sequence = sio.get();

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        String refSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getRef());
        String altSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantInterval, sequence, variant.getAlt());
        if (refSnippet == null || altSnippet == null) {
            // nothing more to be done
            return EMPTY_SVG_IMAGE;
        }

        List<Double> altAcceptorScores = Utils.slidingWindow(altSnippet, splicingParameters.getAcceptorLength())
                .map(icCalculator::getSpliceAcceptorScore)
                .collect(Collectors.toList());

        int altMaxIdx = Utils.argmax(altAcceptorScores);

        // primary - trekker comparing the best ALT window with the corresponding REF window
        String altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
        String refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
        String trekker = vmvtGenerator.getAcceptorTrekkerSvg(refCorrespondingWindow, altBestWindow);

        // secondary - sequence walkers comparing the best ALT window with the canonical acceptor snippet
        GenomePosition acceptorAnchor = predictionData.getMetadata().getAcceptorCoordinateMap().get(predictionData.getTranscript().getAccessionId());
        String walkers;
        if (acceptorAnchor != null) {
            // we have the anchor, thus let's make the graphics
            String canonicalAcceptorSnippet = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorAnchor, variant, sequence);
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

        return makeCrypticContextGraphics(context.getTitle(), trekker, walkers);
    }

    protected String makeSreContextGraphics(SplicingPredictionData predictionData) {
        VisualizationContext context = VisualizationContext.SRE;

        GenomeVariant variant = predictionData.getVariant();
        SequenceInterval sequence = predictionData.getSequence();

        // primary - hexamer
        String hexamerRefSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 5);  // 5 because at least one base is REF/ALT
        String hexamerAltSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 5);
        String hexamerGraphics = hexamerRefSnippet != null && hexamerAltSnippet != null
                ? vmvtGenerator.getHexamerSvg(hexamerRefSnippet, hexamerAltSnippet)
                : null;

        // secondary - heptamer
        String heptamerRefSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 6); // 6 because at least one base is REF/ALT
        String heptamerAltSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 6);
        String heptamerGraphics = heptamerRefSnippet != null && heptamerAltSnippet != null
                ? vmvtGenerator.getHeptamerSvg(heptamerRefSnippet, heptamerAltSnippet)
                : null;

        // at least one figure is present
        if (hexamerGraphics != null || heptamerGraphics != null) {
            return assembleFigures(context, hexamerGraphics, heptamerGraphics);
        }
        return EMPTY_SVG_IMAGE;
    }

    private Optional<SequenceInterval> fetchSequenceForTranscript(SplicingTranscript transcript) {
        return genomeSequenceAccessor.fetchSequence(transcript.getTxRegionCoordinates().withMorePadding(100, 100));
    }
}
