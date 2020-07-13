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
     * @param padding  number of padded bases
     * @return snippet or <code>null</code> if <code>sequence</code> is on different chromosome that the
     * <code>interval</code>, or if not enough sequence is provided
     */
    private static String getPaddedAllele(GenomeInterval interval, SequenceInterval sequence, String allele, int padding) {
        final Optional<String> upstream = sequence.getSubsequence(new GenomeInterval(interval.getGenomeEndPos(), padding));
        final Optional<String> downstream = sequence.getSubsequence(new GenomeInterval(interval.getGenomeBeginPos().shifted(-padding), padding));

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

    /*
                                                  K-MERS
     */

    /**
     * Generate snippet with REF allele and the appropriate amount of neighboring sequence, to use for sliding
     * window to use with k-mer scorers.
     *
     * @param variant  to make the snippet for
     * @param sequence reference fasta sequence
     * @param k        length of the k-mer
     * @return snippet or <code>null</code> if there is not enough sequence available, or sequence and variant are in fact on different chromosomes, etc.
     */
    public String getKmerRefSnippet(GenomeVariant variant, SequenceInterval sequence, int k) {
        final int padding = k - 1;
        final GenomeInterval vi = variant.getGenomeInterval();
        final Optional<String> optUpstream = sequence.getSubsequence(new GenomeInterval(vi.getGenomeBeginPos().shifted(-(padding)), padding));
        final Optional<String> optDownstream = sequence.getSubsequence(new GenomeInterval(vi.getGenomeEndPos(), padding));

        if (optUpstream.isPresent() && optDownstream.isPresent()) {
            return optUpstream.get() + variant.getRef() + optDownstream.get();
        } else {
            return null;
        }
    }

    /**
     * Generate snippet with ALT allele and the appropriate amount of neighboring sequence, to use for sliding
     * window to use with k-mer scorers.
     *
     * @param variant  to make the snippet for
     * @param sequence reference fasta sequence
     * @param k        length of the k-mer
     * @return snippet or <code>null</code> if there is not enough sequence available, or sequence and variant are in fact on different chromosomes, etc.
     */
    public String getKmerAltSnippet(GenomeVariant variant, SequenceInterval sequence, int k) {
        final int padding = k - 1;
        final GenomeInterval vi = variant.getGenomeInterval();
        final Optional<String> optUpstream = sequence.getSubsequence(new GenomeInterval(vi.getGenomeBeginPos().shifted(-(padding)), padding));
        final Optional<String> optDownstream = sequence.getSubsequence(new GenomeInterval(vi.getGenomeEndPos(), padding));

        if (optUpstream.isPresent() && optDownstream.isPresent()) {
            return optUpstream.get() + variant.getAlt() + optDownstream.get();
        } else {
            return null;
        }
    }
}
