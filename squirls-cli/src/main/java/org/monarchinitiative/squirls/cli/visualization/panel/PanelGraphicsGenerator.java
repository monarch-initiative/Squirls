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

package org.monarchinitiative.squirls.cli.visualization.panel;

import de.charite.compbio.jannovar.annotation.Annotation;
import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.cli.visualization.VisualizableVariantAllele;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContextSelector;
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.SquirlsTxResult;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.reference.*;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Variant;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.monarchinitiative.squirls.core.Utils.argmax;
import static org.monarchinitiative.squirls.core.Utils.slidingWindow;

/**
 * This graphics generator makes graphics for the splice variant.
 * @author Daniel Danis
 */
public class PanelGraphicsGenerator implements SplicingVariantGraphicsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PanelGraphicsGenerator.class);

    private static final NumberFormat NF = NumberFormat.getInstance();

    private static final AnnotationComparator TX_COMPARATOR = new AnnotationComparator();

    private final VmvtGenerator vmvtGenerator;

    private final VisualizationContextSelector contextSelector;
    private final SquirlsDataService squirlsDataService;
    private final AlleleGenerator alleleGenerator;
    private final SplicingParameters splicingParameters;
    private final SplicingInformationContentCalculator icCalculator;

    private final TemplateEngine templateEngine;

    private final TranscriptModelLocator locator;

    public PanelGraphicsGenerator(VmvtGenerator vmvtGenerator,
                                  VisualizationContextSelector contextSelector,
                                  SquirlsDataService squirlsDataService,
                                  SplicingPwmData splicingPwmData) {
        this.vmvtGenerator = vmvtGenerator;
        this.contextSelector = contextSelector;
        this.squirlsDataService = squirlsDataService;

        this.splicingParameters = splicingPwmData.getParameters();
        this.alleleGenerator = new AlleleGenerator(splicingParameters);
        this.icCalculator = new SplicingInformationContentCalculator(splicingPwmData);
        this.locator = new TranscriptModelLocatorNaive(splicingPwmData.getParameters());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/panel/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    private StrandedSequence fetchSequenceForTranscript(TranscriptModel transcript) {
        return squirlsDataService.sequenceForRegion(transcript.withPadding(250, 250));
    }

    public String generateGraphics(VisualizableVariantAllele allele) {
        Context context = new Context();
        context.setVariable("squirls_results", allele.squirlsResults());

        List<Annotation> annotations = allele.variantAnnotations().getAnnotations().stream()
                .sorted(TX_COMPARATOR)
                .collect(Collectors.toList());

        context.setVariable("variantAnnotations", annotations); // for tx table

        Optional<SquirlsTxResult> mspOpt = allele.squirlsResults().maxPathogenicityResult();
        if (mspOpt.isEmpty()) {
            return EMPTY_SVG_IMAGE;
        }

        SquirlsTxResult highestPrediction = mspOpt.get();
        context.setVariable("highest_prediction", highestPrediction); // for features table

        Optional<TranscriptModel> stOpt = squirlsDataService.transcriptByAccession(highestPrediction.accessionId());
        if (stOpt.isEmpty()) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Could not find transcript {} for variant {}", highestPrediction.accessionId(), allele.genomeVariant());
            return EMPTY_SVG_IMAGE;
        }
        TranscriptModel transcript = stOpt.get();


        Variant variant = allele.variant().withStrand(transcript.strand()).withCoordinateSystem(transcript.coordinateSystem());

        SplicingLocationData locationData = locator.locate(variant, transcript);
        Optional<GenomicRegion> dOpt = locationData.getDonorRegion();
        Optional<GenomicRegion> aOpt = locationData.getAcceptorRegion();

        String primary = EMPTY_SVG_IMAGE, secondary = EMPTY_SVG_IMAGE;
        String primaryLabel = "", secondaryLabel = "";
        String crypticCoordinate = "";
        int basesChanged = 0;
        context.setVariable("basesChanged", basesChanged);

        String templateName;
        VisualizationContext visualizationContext = contextSelector.selectContext(highestPrediction.features());
        switch (visualizationContext) {
            case CANONICAL_DONOR:
            case CRYPTIC_DONOR:
                templateName = "donor";
                break;
            case CANONICAL_ACCEPTOR:
            case CRYPTIC_ACCEPTOR:
                templateName = "acceptor";
                break;
            case UNKNOWN:
            default:
                templateName = "unknown";
        }
        context.setVariable("context", visualizationContext.getTitle());

        StrandedSequence sequence = fetchSequenceForTranscript(transcript);
        if (sequence == null) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Unable to fetch enough reference sequence for transcript `{}`", transcript.accessionId());
            return templateEngine.process(templateName, context);
        }

        try {
            int altMaxIdx;
            String refAllele, altAllele, refSnippet, altSnippet, altBestWindow, refCorrespondingWindow;
            GenomicRegion donorRegion, acceptorRegion;
            switch (visualizationContext) {
                case CANONICAL_DONOR:
                    // cannot draw anything without knowing the donor border
                    if (dOpt.isEmpty()) break;

                    donorRegion = dOpt.get();

                    // cannot draw if variant does not overlap with the site
                    if (!variant.overlapsWith(donorRegion)) break;

                    refAllele = sequence.subsequence(donorRegion);
                    if (refAllele == null) break;

                    // primary - donor logo, ruler, and bar chart
                    // secondary - donor distribution SVG
                    altAllele = alleleGenerator.getDonorSiteWithAltAllele(donorRegion, variant, sequence);
                    primaryLabel = "Canonical donor site";
                    primary = vmvtGenerator.getDonorSequenceLogoRulerAndBarChart(refAllele, altAllele);

                    secondaryLabel = "<a href=\"https://squirls.readthedocs.io/en/latest/interpretation.html#delta-ri-ref\">" +
                            "<em>&Delta;R<sub>i</sub></em> score distribution</a>";
                    secondary = vmvtGenerator.getDonorDistributionSvg(refAllele, altAllele);
                    break;

                case CANONICAL_ACCEPTOR:
                    // cannot draw anything without knowing the acceptor border
                    if (aOpt.isEmpty()) break;

                    acceptorRegion = aOpt.get();

                    // cannot draw if variant does not overlap with the site
                    if (!variant.overlapsWith(acceptorRegion)) break;

                    refAllele = sequence.subsequence(acceptorRegion);
                    if (refAllele == null) break;

                    // primary - acceptor logo, ruler, and bar chart
                    // secondary - acceptor distribution SVG
                    altAllele = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorRegion, variant, sequence);
                    primaryLabel = "Canonical acceptor site";
                    primary = vmvtGenerator.getAcceptorSequenceLogoRulerAndBarChart(refAllele, altAllele);

                    secondaryLabel = "<a href=\"https://squirls.readthedocs.io/en/latest/interpretation.html#delta-ri-ref\">" +
                            "<em>&Delta;R<sub>i</sub></em> score distribution</a>";
                    secondary = vmvtGenerator.getAcceptorDistributionSvg(refAllele, altAllele);
                    break;

                case CRYPTIC_DONOR:
                    refSnippet = alleleGenerator.getDonorNeighborSnippet(variant, sequence, variant.ref());
                    altSnippet = alleleGenerator.getDonorNeighborSnippet(variant, sequence, variant.alt());
                    if (refSnippet == null || altSnippet == null) break;

                    List<Double> altDonorScores = slidingWindow(altSnippet, splicingParameters.getDonorLength())
                            .map(icCalculator::getSpliceDonorScore)
                            .collect(Collectors.toList());

                    altMaxIdx = argmax(altDonorScores);

                    if (dOpt.isEmpty()) break;

                    donorRegion = dOpt.get();
                    refAllele = sequence.subsequence(donorRegion);
                    altAllele = alleleGenerator.getDonorSiteWithAltAllele(donorRegion, variant, sequence);
                    primaryLabel = "Canonical donor site";
                    primary = vmvtGenerator.getDonorSequenceLogoRulerAndBarChart(refAllele, altAllele);

                    altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());
                    refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getDonorLength());

                    // which position of the cryptic site the variant is located at? (-1 to transform to 0-based)
                    int variantCrypticDonorSiteIdx = splicingParameters.getDonorLength() - altMaxIdx - 1;
                    int donorDiff = Utils.getDiff(variant, donorRegion.start() + splicingParameters.getDonorExonic()) - variantCrypticDonorSiteIdx;
                    basesChanged = donorDiff + splicingParameters.getDonorExonic();

                    secondaryLabel = "Predicted cryptic donor site";
                    secondary = vmvtGenerator.getDonorSequenceRulerAndBarChartWithOffset(refCorrespondingWindow, altBestWindow, basesChanged);
                    int crypticDonorDelta = splicingParameters.getDonorExonic() - variantCrypticDonorSiteIdx;
                    int crypticDonorPos = variant.toPositiveStrand().toOneBased().start() + crypticDonorDelta;

                    crypticCoordinate = String.format("%s:%s", variant.contigName(), NF.format(crypticDonorPos));
                    break;

                case CRYPTIC_ACCEPTOR:
                    refSnippet = alleleGenerator.getAcceptorNeighborSnippet(variant, sequence, variant.ref());
                    altSnippet = alleleGenerator.getAcceptorNeighborSnippet(variant, sequence, variant.alt());
                    if (refSnippet == null || altSnippet == null) break;

                    List<Double> altAcceptorScores = slidingWindow(altSnippet, splicingParameters.getAcceptorLength())
                            .map(icCalculator::getSpliceAcceptorScore)
                            .collect(Collectors.toList());

                    altMaxIdx = argmax(altAcceptorScores);

                    if (aOpt.isEmpty()) break;

                    acceptorRegion = aOpt.get();
                    refAllele = sequence.subsequence(acceptorRegion);
                    altAllele = alleleGenerator.getAcceptorSiteWithAltAllele(acceptorRegion, variant, sequence);

                    primary = vmvtGenerator.getAcceptorSequenceLogoRulerAndBarChart(refAllele, altAllele);
                    primaryLabel = "Canonical acceptor site";

                    altBestWindow = altSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());
                    refCorrespondingWindow = refSnippet.substring(altMaxIdx, altMaxIdx + splicingParameters.getAcceptorLength());

                    // which position of the cryptic site the variant is located at? (-1 to transform to 0-based)
                    int variantCrypticAcceptorSiteIdx = splicingParameters.getAcceptorLength() - altMaxIdx - 1;
                    int acceptorDiff = Utils.getDiff(variant, acceptorRegion.end() - splicingParameters.getAcceptorExonic()) - variantCrypticAcceptorSiteIdx;
                    basesChanged = acceptorDiff + splicingParameters.getAcceptorIntronic();

                    secondary = vmvtGenerator.getAcceptorSequenceRulerAndBarChartWithOffset(refCorrespondingWindow, altBestWindow, basesChanged);
                    secondaryLabel = "Predicted cryptic acceptor site";

                    int crypticAcceptorDelta = splicingParameters.getAcceptorIntronic() - variantCrypticAcceptorSiteIdx;
                    int crypticAcceptorPos = variant.toPositiveStrand().toOneBased().start() + crypticAcceptorDelta;

                    crypticCoordinate = String.format("%s:%s", variant.contigName(), NF.format(crypticAcceptorPos));
                    break;

                case UNKNOWN:
                default:
                    LOGGER.warn("Unable to generate graphics for {} context", visualizationContext.getTitle());
                    return templateEngine.process(templateName, context);
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot generate graphics for {}. {}", allele.genomeVariant(), e.getMessage());
            return templateEngine.process(templateName, context);
        }

        context.setVariable("crypticCoordinate", crypticCoordinate);
        context.setVariable("basesChanged", basesChanged);
        context.setVariable("primary", primary);
        context.setVariable("primaryLabel", primaryLabel);
        context.setVariable("secondary", secondary);
        context.setVariable("secondaryLabel", secondaryLabel);

        return templateEngine.process(templateName, context);
    }
}
