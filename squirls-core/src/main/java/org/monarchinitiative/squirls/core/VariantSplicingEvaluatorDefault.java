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

import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.SquirlsFeatures;
import org.monarchinitiative.squirls.core.reference.StrandedSequence;
import org.monarchinitiative.squirls.core.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class VariantSplicingEvaluatorDefault implements VariantSplicingEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantSplicingEvaluatorDefault.class);

    private static final int PADDING = 150;

    private final SquirlsDataService squirlsDataService;

    private final SplicingAnnotator annotator;

    private final SquirlsClassifier classifier;

    private VariantSplicingEvaluatorDefault(SquirlsDataService squirlsDataService,
                                            SplicingAnnotator annotator,
                                            SquirlsClassifier classifier) {
        this.squirlsDataService = Objects.requireNonNull(squirlsDataService, "Squirls data service cannot be null");
        this.annotator = Objects.requireNonNull(annotator, "Splicing Annotator cannot be null");
        this.classifier = Objects.requireNonNull(classifier, "Squirls classifier cannot be null");
    }

    static VariantSplicingEvaluatorDefault of(SquirlsDataService squirlsDataService,
                                              SplicingAnnotator annotator,
                                              SquirlsClassifier classifier) {
        return new VariantSplicingEvaluatorDefault(squirlsDataService, annotator, classifier);
    }

    /**
     * Evaluate given variant with respect to transcripts in <code>txIds</code>. The method <em>attempts</em> to evaluate
     * the variant with respect to given <code>txIds</code>, but does not guarantee that results will be provided for
     * each transcript.
     * <p>
     * Note that only transcripts with at least 2 exons are considered.
     *
     * @param txIds set of transcript accession IDs with respect to which the variant should be evaluated
     * @return possibly empty map with {@link SquirlsResult} for transcript ID
     */
    @Override
    public SquirlsResult evaluate(Variant variant, Set<String> txIds) {
        if (VariantType.isSymbolic(variant.ref(), variant.alt())) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Skipping symbolic variant {}:{}-{} {}", variant.contigName(), variant.start(), variant.end(), variant.variantType());
            return SquirlsResult.empty();
        } else if (VariantType.isMissingUpstreamDeletion(variant.alt())) {
            // VCF4.2 specs:
            // The ‘*’ allele is reserved to indicate that the allele is missing due to a upstream deletion.
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Skipping variant where alt allele is missing due to an upstream deletion: {}:{}-{} {}",
                        variant.contigName(), variant.startPositionWithCoordinateSystem(CoordinateSystem.oneBased()), variant.ref(), variant.alt());
            return SquirlsResult.empty();
        }

        /*
         0 - perform some sanity checks at the beginning.
         */
        if (!squirlsDataService.knownContigNames().contains(variant.contigName())) {
            // TODO - check if the contig accession matches
            // unknown contig, nothing to be done here
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Unknown contig for variant {}:{}{}>{}", variant.contigName(), variant.start(), variant.ref(), variant.alt());
            return SquirlsResult.empty();
        }

        /*
         1 - get overlapping splicing transcripts. Query by coordinates if no txIDs are provided. Only transcripts with
         two or more exons that overlap with the variant interval are considered.
         */
        Map<String, TranscriptModel> txMap = fetchTranscripts(variant, txIds);

        if (txMap.isEmpty()) {
            // shortcut, no transcripts to evaluate
            return SquirlsResult.empty();
        }

        /*
         2 - get enough reference sequence for evaluation with respect to all transcripts. The sequence is queried
         on POSITIVE strand using 0-based coordinate system.
         */
        Strand strand = Strand.POSITIVE;
        CoordinateSystem coordinateSystem = CoordinateSystem.zeroBased();
        Integer bp = null, ep = null;
        for (TranscriptModel tx : txMap.values()) {
            GenomicRegion txIntervalFwd = tx.withStrand(strand).withCoordinateSystem(coordinateSystem);
            int start = txIntervalFwd.start();
            if (bp == null || bp > start) {
                bp = start;
            }
            int end = txIntervalFwd.end();
            if (ep == null || ep < end) {
                ep = end;
            }
        }

        // PADDING should provide enough sequence in most cases
        bp -= PADDING;
        ep += PADDING;
        GenomicRegion toFetch = GenomicRegion.of(variant.contig(), strand, coordinateSystem, Position.of(bp), Position.of(ep));
        StrandedSequence seq = squirlsDataService.sequenceForRegion(toFetch);
        if (seq == null) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unable to get reference sequence for `{}` when evaluating variant `{}`", toFetch, variant);
            return SquirlsResult.empty();
        }

        /*
         3 - let's evaluate the variant with respect to all transcripts
         */
        Set<SquirlsTxResult> squirlsTxResults = txMap.keySet().stream()
                .map(tx -> {
                    VariantOnTranscript vtx = VariantOnTranscript.of(variant, txMap.get(tx), seq);
                    SquirlsFeatures annotations = annotator.annotate(vtx);
                    Prediction prediction = classifier.predict(annotations);
                    return SquirlsTxResult.of(tx, prediction, annotations.getFeatureMap());
                })
                .collect(Collectors.toSet());
        return SquirlsResult.of(squirlsTxResults);
    }

    /**
     * Use provided variant coordinates <em>OR</em> transcript accession IDs to fetch {@link TranscriptModel}s from
     * the database.
     * <p>
     * Only transcripts consisting of 2 or more exons that overlap with the <code>variant</code> are returned.
     * </p>
     *
     * @param variant {@link Variant} with variant coordinates
     * @param txIds   set of transcript accession IDs
     * @return map with transcripts group
     */
    private Map<String, TranscriptModel> fetchTranscripts(GenomicRegion variant, Set<String> txIds) {
        Map<String, TranscriptModel> txMap = new HashMap<>();
        variant = variant.toPositiveStrand().toZeroBased();

        if (txIds.isEmpty()) {
            // querying by coordinates
            return squirlsDataService.overlappingTranscripts(variant).stream()
                    .filter(st -> !st.introns().isEmpty())
                    .collect(Collectors.toMap(TranscriptModel::accessionId, Function.identity()));
        } else {
            // or query by transcript IDs
            for (String txId : txIds) {
                Optional<TranscriptModel> sto = squirlsDataService.transcriptByAccession(txId);
                if (sto.isPresent()) {
                    TranscriptModel st = sto.get();
                    // the transcript
                    //  - has 2+ exons
                    //  - overlaps with the variant
                    if (!st.introns().isEmpty() && st.overlapsWith(variant)) {
                        txMap.put(txId, st);
                    }
                } else {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Unknown transcript id `{}` for variant {}", txId, variant);
                }
            }
        }
        return txMap;
    }

}
