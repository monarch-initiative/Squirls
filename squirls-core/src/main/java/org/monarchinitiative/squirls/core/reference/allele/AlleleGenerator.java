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

package org.monarchinitiative.squirls.core.reference.allele;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Optional;

/**
 * Class that creates nucleotide snippets for splice donor or acceptor site while incorporating alternate allele
 * into the snippet.
 * <p>
 * Obviously, it is trivial to create a snippet for SNP. It's harder for INDELs, that's the main task of this class.
 * </p>
 */
public class AlleleGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlleleGenerator.class);


    private final SplicingParameters splicingParameters;

    public AlleleGenerator(SplicingParameters splicingParameters) {
        this.splicingParameters = splicingParameters;
    }

    /**
     * Get snippet of neighboring sequence for <code>allele</code> located at given <code>interval</code>. The
     * neighboring sequence includes <code>+-padding</code> bases upstream and downstream.
     *
     * @param interval spanned by the variant
     * @param sequence reference FASTA sequence
     * @param allele   string with allele
     * @param padding  non-negative number of padded bases
     * @return snippet or <code>null</code> if <code>sequence</code> is on different chromosome that the
     * <code>interval</code>, or if not enough sequence is provided
     */
    public static String getPaddedAllele(GenomeInterval interval, SequenceInterval sequence, String allele, int padding) {
        if (padding < 0) return null;

        final Optional<String> upstream = sequence.getSubsequence(new GenomeInterval(interval.getGenomeBeginPos().shifted(-padding), padding));
        final Optional<String> downstream = sequence.getSubsequence(new GenomeInterval(interval.getGenomeEndPos(), padding));

        return upstream.isPresent() && downstream.isPresent()
                ? upstream.get() + allele + downstream.get()
                : null;
    }

    /**
     * Create nucleotide snippet for splice donor site.
     *
     * @param anchor           position of `exon|intron` boundary
     * @param sequenceInterval sequence to use for creating snippet
     * @return wt nucleotide snippet for splice donor site or <code>null</code> if e.g. <code>sequenceInterval</code>
     * on different contig is provided
     */
    public String getDonorSiteSnippet(GenomePosition anchor, SequenceInterval sequenceInterval) {
        return sequenceInterval.getSubsequence(splicingParameters.makeDonorRegion(anchor)).orElse(null);
    }

    /**
     * @param anchor position of `exon|intron` boundary
     * @return interval of splice donor site
     */
    public GenomeInterval makeDonorInterval(GenomePosition anchor) {
        return splicingParameters.makeDonorRegion(anchor);
    }

    /**
     * Create nucleotide snippet for splice acceptor site.
     *
     * @param anchor           position of `intron|exon` boundary
     * @param sequenceInterval sequence to use for creating snippet
     * @return wt nucleotide snippet for splice acceptor site or <code>null</code> if e.g. <code>sequenceInterval</code>
     * on different contig is provided
     */
    public String getAcceptorSiteSnippet(GenomePosition anchor, SequenceInterval sequenceInterval) {
        return sequenceInterval.getSubsequence(splicingParameters.makeAcceptorRegion(anchor)).orElse(null);
    }

    /**
     * @param anchor position of `intron|exon` boundary
     * @return interval of splice acceptor site
     */
    public GenomeInterval makeAcceptorInterval(GenomePosition anchor) {
        return splicingParameters.makeAcceptorRegion(anchor);
    }

    /**
     * Create nucleotide snippet for splice donor site with presence of ALT allele.
     *
     * @param anchor           {@link GenomePosition} denoting exon|intron boundary
     * @param variant          variant to incorporate into the snippet
     * @param sequenceInterval sequence to use for creating snippet
     * @return nucleotide snippet for splice donor site or <code>null</code> if wrong input is provided
     */
    public String getDonorSiteWithAltAllele(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        try {
            String result; // this method creates the result String in 5' --> 3' direction

            if (anchor.getChr() != variant.getChr() || anchor.getChr() != sequenceInterval.getInterval().getChr()) {
                // sanity check
                LOGGER.warn("Chromosome mismatch - anchor: `{}`, variant: `{}`, sequence: `{}`",
                        anchor.getChr(), variant.getChr(), sequenceInterval.getInterval().getChr());
                return null;
            }

            // adjust variant to transcript's strand
            if (!anchor.getStrand().equals(variant.getGenomePos().getStrand())) {
                variant = variant.withStrand(anchor.getStrand());
            }

            final GenomeInterval donor = splicingParameters.makeDonorRegion(anchor);

            if (!donor.overlapsWith(variant.getGenomeInterval())) {
                // shortcut, return wt donor sequence since variant does not change the site
                return sequenceInterval.getSubsequence(donor)
                        .orElse(null);
            }

            final GenomeInterval variantInterval = variant.getGenomeInterval();
            if (variantInterval.contains(donor)) {
                // whole donor site is deleted, nothing more to be done here
                return null;
            }

            String alt = variant.getAlt();
            if (variantInterval.contains(donor.getGenomeBeginPos())) {
                // there will be no upstream region, even some nucleotides from before the variantRegion region will be added,
                // if the first positions of donor site are deleted
                int idx = variantInterval.getEndPos() - donor.getBeginPos();
                result = alt.substring(alt.length() - Math.min(idx, alt.length()));
                // there should be already 'idx' nucleotides in 'result' string but there are only 'missing' nucleotides there. Perhaps because the variantRegion is a deletion
                // therefore, we need to add some nucleotides from region before the variantRegion
                int missing = idx - result.length();
                if (missing > 0) {
                    final GenomeInterval interval = new GenomeInterval(variantInterval.getGenomeBeginPos().shifted(-missing), missing);
                    final Optional<String> opSeq = sequenceInterval.getSubsequence(interval);
                    if (opSeq.isEmpty()) {
                        LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`",
                                variant, sequenceInterval.getInterval(), interval);
                        return null;
                    }
                    result = opSeq.get() + result;
                }
            } else {
                // simple scenario when we just add bases between donor beginning and variant beginning
                final GenomeInterval interval = new GenomeInterval(donor.getGenomeBeginPos(), variantInterval.getGenomeBeginPos().differenceTo(donor.getGenomeBeginPos()));

                final Optional<String> opSeq = sequenceInterval.getSubsequence(interval);

                if (opSeq.isEmpty()) {
                    LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`",
                            variant, sequenceInterval.getInterval(), interval);
                    return null;
                }
                result = opSeq.get() + alt;

            }
            // add nothing if the sequence is already longer than the SPLICE_DONOR_SITE_LENGTH
            GenomePosition dwnsBegin = variantInterval.getGenomeEndPos();
            GenomePosition dwnsEnd = variantInterval.getGenomeEndPos().shifted(Math.max(splicingParameters.getDonorLength() - result.length(), 0));
            final GenomeInterval interval = new GenomeInterval(dwnsBegin, dwnsEnd.differenceTo(dwnsBegin));

            final Optional<String> opSeq = sequenceInterval.getSubsequence(interval);
            if (opSeq.isEmpty()) {
                LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`",
                        variant, sequenceInterval.getInterval(), interval);
                return null;
            }
            result += opSeq.get();
                /* if the variantRegion is a larger insertion, result.length() might be greater than SPLICE_DONOR_SITE_LENGTH after
                   appending 'alt' sequence. We need to make sure only 'SPLICE_DONOR_SITE_LENGTH' nucleotides are returned */
            return result.substring(0, splicingParameters.getDonorLength());
        } catch (StringIndexOutOfBoundsException e) {
            LOGGER.error("Error: ", e);
        }
        return null;
    }

    /**
     * Create nucleotide snippet for splice acceptor site.
     *
     * @param anchor           {@link GenomePosition} denoting intron|exon boundary
     * @param variant          variant to incorporate into the snippet
     * @param sequenceInterval sequence to use for creating snippet
     * @return nucleotide snippet for splice acceptor site or <code>null</code> if wrong input is provided
     */
    public String getAcceptorSiteWithAltAllele(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        String result; // this method creates the result String in 3' --> 5' direction

        if (anchor.getChr() != variant.getChr() || anchor.getChr() != sequenceInterval.getInterval().getChr()) {
            // sanity check
            LOGGER.warn("Chromosome mismatch - anchor: `{}`, variant: `{}`, sequence: `{}`",
                    anchor.getChr(), variant.getChr(), sequenceInterval.getInterval().getChr());
            return null;
        }

        // adjust variant to transcript's strand
        if (!anchor.getStrand().equals(variant.getGenomePos().getStrand())) {
            variant = variant.withStrand(anchor.getStrand());
        }

        final GenomeInterval acceptor = splicingParameters.makeAcceptorRegion(anchor);

        if (!acceptor.overlapsWith(variant.getGenomeInterval())) {
            // shortcut, return wt acceptor sequence since variant does not change the site
            return sequenceInterval.getSubsequence(acceptor)
                    .orElse(null);
        }

        final GenomeInterval variantInterval = variant.getGenomeInterval();
        if (variantInterval.contains(acceptor)) {
            // whole acceptor site is deleted, nothing more to be done here
            return null;
        }

        final String alt = variant.getAlt();
        if (variantInterval.contains(acceptor.getGenomeEndPos())) {
            // we should have at least 'idx' nucleotides in result after writing 'alt' but it might be less if there
            // is a deletion. Write 'alt' to result
            int idx = acceptor.getEndPos() - variantInterval.getBeginPos();
            result = alt.substring(Math.min(idx, alt.length()));

            int missing = idx - result.length();
            if (missing > 0) {
                final GenomeInterval interval = new GenomeInterval(variantInterval.getGenomeEndPos(), missing);
                final Optional<String> opSeq = sequenceInterval.getSubsequence(interval);
                if (opSeq.isEmpty()) {
                    LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`",
                            variant, sequenceInterval.getInterval(), interval);
                    return null;
                }
                result = result + opSeq.get();
            }
        } else {
            final GenomeInterval interval = new GenomeInterval(variantInterval.getGenomeEndPos(), acceptor.getGenomeEndPos().differenceTo(variantInterval.getGenomeEndPos()));
            final Optional<String> opSeq = sequenceInterval.getSubsequence(interval);
            if (opSeq.isEmpty()) {
                LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`",
                        variant, sequenceInterval.getInterval(), interval);
                return null;
            }
            result = alt + opSeq.get();
        }

        // add nothing if the sequence is already longer than the SPLICE_ACCEPTOR_SITE_LENGTH
        int encore = Math.max(splicingParameters.getAcceptorLength() - result.length(), 0); // running out of English words :)
        final GenomeInterval interval = new GenomeInterval(variantInterval.getGenomeBeginPos().shifted(-encore), encore);

        final Optional<String> opSeq = sequenceInterval.getSubsequence(interval);
        if (opSeq.isEmpty()) {
            LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`",
                    variant, sequenceInterval.getInterval(), interval);
            return null;
        }
        result = opSeq.get() + result;
                /* if the variantRegion is a larger insertion, result.length() might be greater than SPLICE_ACCEPTOR_SITE_LENGTH after
                   appending 'alt' sequence. We need to make sure only last 'SPLICE_ACCEPTOR_SITE_LENGTH' nucleotides are returned */
        return result.substring(result.length() - splicingParameters.getAcceptorLength()); // last 'SPLICE_ACCEPTOR_SITE_LENGTH' nucleotides
    }

    /**
     * Get snippet of neighboring sequence for <code>allele</code> located at given <code>interval</code>. The
     * neighboring sequence includes <code>+-donor_length-1</code> bases upstream and downstream.
     *
     * @param interval spanned by the variant
     * @param sequence reference FASTA sequence
     * @param allele   string with allele
     * @return snippet or <code>null</code> if <code>sequence</code> is on different chromosome that the
     * <code>interval</code>, or if not enough sequence is provided
     */
    public String getDonorNeighborSnippet(GenomeInterval interval, SequenceInterval sequence, String allele) {
        return getPaddedAllele(interval, sequence, allele, splicingParameters.getDonorLength() - 1);
    }

    /**
     * Get snippet of neighboring sequence for <code>allele</code> located at given <code>interval</code>. The
     * neighboring sequence includes <code>+-acceptor_length-1</code> bases upstream and downstream.
     *
     * @param interval spanned by the variant
     * @param sequence reference FASTA sequence
     * @param allele   string with allele
     * @return snippet or <code>null</code> if <code>sequence</code> is on different chromosome that the
     * <code>interval</code>, or if not enough sequence is provided
     */
    public String getAcceptorNeighborSnippet(GenomeInterval interval, SequenceInterval sequence, String allele) {
        return getPaddedAllele(interval, sequence, allele, splicingParameters.getAcceptorLength() - 1);
    }
}
