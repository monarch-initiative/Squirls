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

package org.monarchinitiative.squirls.core;

import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.SquirlsFeatures;
import org.monarchinitiative.squirls.core.config.TranscriptCategory;
import org.monarchinitiative.squirls.core.reference.StrandedSequence;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Daniel Danis
 */
class VariantSplicingEvaluatorImpl implements VariantSplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantSplicingEvaluatorImpl.class);

    private static final int PADDING = 150;

    private final SquirlsDataService squirlsDataService;

    private final SplicingAnnotator annotator;
    private final SquirlsClassifier classifier;

    static VariantSplicingEvaluatorImpl of(SquirlsDataService squirlsDataService,
                                           SplicingAnnotator annotator,
                                           SquirlsClassifier classifier) {
        return new VariantSplicingEvaluatorImpl(squirlsDataService,
                annotator,
                classifier);
    }

    private VariantSplicingEvaluatorImpl(SquirlsDataService squirlsDataService,
                                         SplicingAnnotator annotator,
                                         SquirlsClassifier classifier) {
        this.squirlsDataService = Objects.requireNonNull(squirlsDataService, "Squirls data service cannot be null");
        this.annotator = Objects.requireNonNull(annotator, "Splicing Annotator cannot be null");
        this.classifier = Objects.requireNonNull(classifier, "Squirls classifier cannot be null");
    }

    /**
     * Evaluate given variant with respect to all overlapping transcripts. The method <em>attempts</em> to evaluate
     * the variant with respect to each overlapping transcript, but does not guarantee that a result will be provided for
     * each transcript.
     * <p>
     * Note that only transcripts with at least 2 exons are considered.
     *
     * @return {@link SquirlsResult} with the available predictions
     */
    @Override
    public SquirlsResult evaluate(GenomicVariant variant) {
        if (VariantType.isSymbolic(variant.ref(), variant.alt())) {
            LOGGER.debug("Skipping symbolic variant {}:{}-{} {}", variant.contigName(), variant.start(), variant.end(), variant.variantType());
            return SquirlsResult.empty();
        } else if (VariantType.isMissingUpstreamDeletion(variant.alt())) {
            // VCF4.2 specs:
            // The ‘*’ allele is reserved to indicate that the allele is missing due to a upstream deletion.
            LOGGER.debug("Skipping variant where alt allele is missing due to an upstream deletion: {}:{}-{} {}",
                    variant.contigName(), variant.startWithCoordinateSystem(CoordinateSystem.oneBased()), variant.ref(), variant.alt());
            return SquirlsResult.empty();
        }

        /*
         0 - perform some sanity checks at the beginning.
         */
        if (!squirlsDataService.genomicAssembly().containsContig(variant.contig())) {
            // unknown contig, nothing to be done here
            LOGGER.warn("Unknown contig for variant {}:{}{}>{}", variant.contigName(), variant.start(), variant.ref(), variant.alt());
            return SquirlsResult.empty();
        }

        /*
         1 - get the overlapping genes and select the relevant transcripts.
         */
        List<? extends Transcript> transcripts = squirlsDataService.overlappingGenes(variant).stream()
                .flatMap(Gene::transcriptStream)
                .collect(Collectors.toList());

        if (transcripts.isEmpty()) {
            // a shortcut, no transcripts to evaluate
            return SquirlsResult.empty();
        }

        /*
         2 - get enough reference sequence for evaluation with respect to all transcripts. The sequence is queried
         on POSITIVE strand using 0-based coordinate system.
         */
        Strand strand = Strand.POSITIVE;
        Integer bp = null, ep = null;
        for (Transcript tx : transcripts) {
            int start = tx.startOnStrandWithCoordinateSystem(strand, CoordinateSystem.zeroBased());
            if (bp == null || bp > start) {
                bp = start;
            }
            int end = tx.endOnStrandWithCoordinateSystem(strand, CoordinateSystem.zeroBased());
            if (ep == null || ep < end) {
                ep = end;
            }
        }

        // PADDING should provide enough sequence in most cases
        bp -= PADDING;
        ep += PADDING;
        GenomicRegion toFetch = GenomicRegion.of(variant.contig(), strand, Coordinates.of(CoordinateSystem.zeroBased(), bp, ep));
        StrandedSequence seq = squirlsDataService.sequenceForRegion(toFetch);
        if (seq == null) {
            LOGGER.debug("Unable to get reference sequence for `{}` when evaluating variant `{}`", toFetch, variant);
            return SquirlsResult.empty();
        }

        /*
         3 - let's evaluate the variant with respect to all transcripts
         */
        List<SquirlsTxResult> squirlsTxResults = transcripts.stream()
                .map(tx -> {
                    VariantOnTranscript vtx = VariantOnTranscript.of(variant, tx, seq);
                    SquirlsFeatures annotations = annotator.annotate(vtx);
                    Prediction prediction = classifier.predict(annotations);
                    return SquirlsTxResult.of(tx.accession(), prediction, annotations.getFeatureMap());
                })
                .collect(Collectors.toUnmodifiableList());
        return SquirlsResult.of(squirlsTxResults);
    }


}
