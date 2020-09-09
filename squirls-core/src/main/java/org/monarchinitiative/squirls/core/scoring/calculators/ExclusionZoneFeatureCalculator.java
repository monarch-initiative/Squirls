package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.regex.Pattern;

/**
 * This calculator returns <code>1</code> if variant creates novel <code>AG</code> di-nucleotide within AG exclusion zone
 * (AGEZ).
 * <p>
 * To simplify things, AGEZ is not defined specifically for each transcript, but as a region spanning (-51,-3] positions
 * upstream from the acceptor site.
 */
public class ExclusionZoneFeatureCalculator extends BaseAgezCalculator {

    /**
     * Default AGEZ begin and end coordinates.
     */
    public static final int AGEZ_BEGIN = -51, AGEZ_END = -3;

    /**
     * This pattern matches nucleotide string if it contains `AG` di-nucleotide.
     */
    private static final Pattern AG_PATTERN = Pattern.compile("^[acgt]*ag[actg]*$");

    /**
     * This pattern matches nucleotide string if it contains `YAG` di-nucleotide where `Y` represents pyrimidine.
     */
    private static final Pattern YAG_PATTERN = Pattern.compile("^[acgt]*[ct]ag[actg]*$");

    private final Pattern pattern;

    /**
     * By specifying a different {@code pattern} we can implement
     *
     * @param locator   locator instance to determine relative position of variant with respect to transcript
     * @param agezBegin 0-based (excluded) begin position of AGEZ with respect to intron|exon boundary, e.g -51
     * @param agezEnd   0-based (included) end position of AGEZ with respect to intron|exon boundary, e.g. -3
     * @param pattern   pattern to use for matching the acceptor snippet
     */
    private ExclusionZoneFeatureCalculator(SplicingTranscriptLocator locator, int agezBegin, int agezEnd, Pattern pattern) {
        super(locator, agezBegin, agezEnd);
        this.pattern = pattern;
    }

    /**
     * @param locator   locator instance to determine relative position of variant with respect to transcript
     * @param agezBegin 0-based (excluded) begin position of AGEZ with respect to intron|exon boundary, e.g -51
     * @param agezEnd   0-based (included) end position of AGEZ with respect to intron|exon boundary, e.g. -3
     */
    public static ExclusionZoneFeatureCalculator makeAgCalculator(SplicingTranscriptLocator locator, int agezBegin, int agezEnd) {
        return new ExclusionZoneFeatureCalculator(locator, agezBegin, agezEnd, AG_PATTERN);
    }

    /**
     * Get calculator that
     *
     * @param locator   locator instance to determine relative position of variant with respect to transcript
     * @param agezBegin 0-based (excluded) begin position of AGEZ with respect to intron|exon boundary, e.g -51
     * @param agezEnd   0-based (included) end position of AGEZ with respect to intron|exon boundary, e.g. -3
     */
    public static ExclusionZoneFeatureCalculator makeYagCalculator(SplicingTranscriptLocator locator, int agezBegin, int agezEnd) {
        return new ExclusionZoneFeatureCalculator(locator, agezBegin, agezEnd, YAG_PATTERN);
    }

    /**
     * Calculate feature value for given variant and transcript.
     *
     * @param variant    variant we calculate the feature for
     * @param transcript transcript we evaluate the variant against
     * @param sequence   FASTA sequence for the calculation
     * @return <code>1.</code> if variant creates a new <code>AG</code> di-nucleotide within the <em>AG exclusion zone</em>
     * and <code>0.</code> otherwise. Note that {@link Double#NaN} is returned if <code>sequence</code> does not contain
     * sufficient nucleotide sequence
     */
    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        if (!overlapsWithAgezRegion(variant, transcript)) {
            return 0.;
        }

        // get alleles padded by 1 bp from each side
        final String refAllele = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 2);
        final String altAllele = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 2);
        if (refAllele == null || altAllele == null) {
            // unable to create padded alleles due to insufficient sequence. This should not happen since we fetch
            // the entire sequence of the transcript region +- padding
            return Double.NaN;
        }

        /*
        A new AG di-nucleotide is created if altAllele is a match while refAllele is not
         */
        return pattern.matcher(refAllele.toLowerCase()).matches() ^ pattern.matcher(altAllele.toLowerCase()).matches()
                ? 1.
                : 0.;
    }
}
