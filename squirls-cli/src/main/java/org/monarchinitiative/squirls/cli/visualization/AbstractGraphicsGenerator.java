/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

package org.monarchinitiative.squirls.cli.visualization;

import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContextSelector;
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.reference.*;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.variant.api.GenomicPosition;
import org.monarchinitiative.variant.api.GenomicRegion;
import org.monarchinitiative.variant.api.Variant;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractGraphicsGenerator implements SplicingVariantGraphicsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphicsGenerator.class);

    protected final VmvtGenerator vmvtGenerator;

    protected final VisualizationContextSelector contextSelector;
    protected final SquirlsDataService squirlsDataService;
    private final AlleleGenerator alleleGenerator;
    private final SplicingParameters splicingParameters;
    private final SplicingInformationContentCalculator icCalculator;

    protected AbstractGraphicsGenerator(VmvtGenerator vmvtGenerator,
                                        SplicingPwmData splicingPwmData,
                                        VisualizationContextSelector contextSelector,
                                        SquirlsDataService squirlsDataService) {

        this.vmvtGenerator = vmvtGenerator;
        this.splicingParameters = splicingPwmData.getParameters();
        this.alleleGenerator = new AlleleGenerator(splicingPwmData.getParameters());
        this.icCalculator = new SplicingInformationContentCalculator(splicingPwmData);
        this.contextSelector = contextSelector;
        this.squirlsDataService = squirlsDataService;
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
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCanonicalDonorContextGraphics(Variant variant,
                                                       TranscriptModel transcript,
                                                       GenomicPosition donorAnchor) {
        VisualizationContext context = VisualizationContext.CANONICAL_DONOR;

        StrandedSequence sequence = fetchSequenceForTranscript(transcript);
        if (sequence == null) {
            return EMPTY_SVG_IMAGE;
        }

        // Overlaps with canonical donor site?
        if (donorAnchor != null) {
            GenomicRegion canonicalDonorInterval = alleleGenerator.makeDonorInterval(donorAnchor);
            if (variant.overlapsWith(canonicalDonorInterval)) {
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
                            .append(vmvtGenerator.getDonorIcBarsWithRi(refAllele, altAllele))
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
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCanonicalAcceptorContextGraphics(Variant variant,
                                                          TranscriptModel transcript,
                                                          GenomicPosition acceptorAnchor) {
        VisualizationContext context = VisualizationContext.CANONICAL_ACCEPTOR;

        StrandedSequence sequence = fetchSequenceForTranscript(transcript);
        if (sequence == null) {
            return EMPTY_SVG_IMAGE;
        }

        // Overlaps with canonical acceptor site?
        if (acceptorAnchor != null) {
            GenomicRegion canonicalAcceptorInterval = alleleGenerator.makeAcceptorInterval(acceptorAnchor);
            if (variant.overlapsWith(canonicalAcceptorInterval)) {
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
                            .append(vmvtGenerator.getAcceptorIcBarsWithRi(refAllele, altAllele))
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
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCrypticDonorContextGraphics(Variant variant,
                                                     TranscriptModel transcript,
                                                     GenomicPosition donorAnchor) {
        VisualizationContext context = VisualizationContext.CRYPTIC_DONOR;

        StrandedSequence sequence = fetchSequenceForTranscript(transcript);
        if (sequence == null) {
            return EMPTY_SVG_IMAGE;
        }

        GenomicRegion variantRegion = variant.withStrand(transcript.strand());

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        String refSnippet = alleleGenerator.getDonorNeighborSnippet(variantRegion, sequence, variant.ref());
        String altSnippet = alleleGenerator.getDonorNeighborSnippet(variantRegion, sequence, variant.alt());
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
        String trekker = vmvtGenerator.getDonorIcBarsWithRi(refCorrespondingWindow, altBestWindow);

        // secondary - sequence walkers comparing the best ALT window with the canonical donor snippet
        // TODO - update with the other IC bars
        String walkers = "";
        if (donorAnchor != null) {
            // we have the anchor, thus let's make the graphics
            String canonicalDonorSnippet = alleleGenerator.getDonorSiteWithAltAllele(donorAnchor, variant, sequence);
//            walkers = vmvtGenerator.getDonorCanonicalCryptic(canonicalDonorSnippet, altBestWindow);
        } else {
            // there is no anchor, this happens in single-exon transcripts
            if (!transcript.introns().isEmpty()) {
                // however, complain if this is not a single-exon transcript!
                LOGGER.warn("Did not find donor site in metadata while but the transcript has {} intron(s)", transcript.intronCount());
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
     * @return String with content in HTML format containing SVG with graphics
     */
    protected String makeCrypticAcceptorContextGraphics(Variant variant,
                                                        TranscriptModel transcript,
                                                        GenomicPosition acceptorAnchor) {
        VisualizationContext context = VisualizationContext.CRYPTIC_ACCEPTOR;

        StrandedSequence sequence = fetchSequenceForTranscript(transcript);
        if (sequence == null) {
            return EMPTY_SVG_IMAGE;
        }

        GenomicRegion variantRegion = variant.withStrand(transcript.strand());

        // find index of the position that yields the highest score
        // get the corresponding ref & alt snippets
        String refSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantRegion, sequence, variant.ref());
        String altSnippet = alleleGenerator.getAcceptorNeighborSnippet(variantRegion, sequence, variant.alt());
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
        String trekker = vmvtGenerator.getAcceptorIcBarsWithRi(refCorrespondingWindow, altBestWindow);

        // secondary - sequence walkers comparing the best ALT window with the canonical acceptor snippet
        // TODO - update with the other IC bars
        String walkers = "";
        if (acceptorAnchor != null) {
            // we have the anchor, thus let's make the graphics
            String canonicalAcceptorSnippet = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorAnchor, variant, sequence);
//            walkers = vmvtGenerator.getAcceptorCanonicalCryptic(canonicalAcceptorSnippet, altBestWindow);
        } else {
            // there is no anchor, this happens in single-exon transcripts
            if (!transcript.introns().isEmpty()) {
                // however, complain if this is not single-exon transcript!
                LOGGER.warn("Did not find acceptor site in metadata while but the transcript has {} intron(s)",
                        transcript.intronCount());
            }
            walkers = null;
        }

        return makeCrypticContextGraphics(context.getTitle(), trekker, walkers);
    }

    // TODO - resolve
//    protected String makeSreContextGraphics(VariantOnTranscript predictionData) {
//        VisualizationContext context = VisualizationContext.SRE;
//
//        GenomeVariant variant = predictionData.variant();
//        SequenceInterval sequence = predictionData.sequence();
//
//        // primary - hexamer
//        String hexamerRefSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 5);  // 5 because at least one base is REF/ALT
//        String hexamerAltSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 5);
//        String hexamerGraphics = hexamerRefSnippet != null && hexamerAltSnippet != null
//                ? vmvtGenerator.getHexamerSvg(hexamerRefSnippet, hexamerAltSnippet)
//                : null;
//
//        // secondary - heptamer
//        String heptamerRefSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 6); // 6 because at least one base is REF/ALT
//        String heptamerAltSnippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 6);
//        String heptamerGraphics = heptamerRefSnippet != null && heptamerAltSnippet != null
//                ? vmvtGenerator.getHeptamerSvg(heptamerRefSnippet, heptamerAltSnippet)
//                : null;
//
//        // at least one figure is present
//        if (hexamerGraphics != null || heptamerGraphics != null) {
//            return assembleFigures(context, hexamerGraphics, heptamerGraphics);
//        }
//        return EMPTY_SVG_IMAGE;
//    }

    private StrandedSequence fetchSequenceForTranscript(TranscriptModel transcript) {
        return squirlsDataService.sequenceForRegion(transcript.withPadding(100, 100));
    }
}
