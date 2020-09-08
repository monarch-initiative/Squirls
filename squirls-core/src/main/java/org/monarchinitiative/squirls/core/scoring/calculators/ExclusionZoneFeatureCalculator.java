package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
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
public class ExclusionZoneFeatureCalculator implements FeatureCalculator {

    /**
     * This pattern matches nucleotide string if it contains `AG` dinucleotide.
     */
    private static final Pattern AG_PATTERN = Pattern.compile("^[acgt]*ag[actg]*$", Pattern.CASE_INSENSITIVE);

    /**
     * Default AGEZ begin and end coordinates.
     */
    private static final int AGEZ_BEGIN = -51, AGEZ_END = -3;


    private final SplicingTranscriptLocator locator;

    private final int agezBegin, agezEnd;

    /**
     * Create the calculator using default AGEZ begin/end coordinates.
     */
    public ExclusionZoneFeatureCalculator(SplicingTranscriptLocator locator) {
        this(locator, AGEZ_BEGIN, AGEZ_END);
    }

    /**
     * Create the calculator.
     *
     * @param locator   locator instance to determine relative position of variant with respect to transcript
     * @param agezBegin 0-based (excluded) begin position of AGEZ with respect to intron|exon boundary, e.g -51
     * @param agezEnd   0-based (included) end position of AGEZ with respect to intron|exon boundary, e.g. -3
     */
    public ExclusionZoneFeatureCalculator(SplicingTranscriptLocator locator, int agezBegin, int agezEnd) {
        this.locator = locator;
        this.agezBegin = agezBegin;
        this.agezEnd = agezEnd;
    }

    /**
     * Calculate feature value for given variant and transcript.
     *
     * @param variant    variant we calculate the feature for
     * @param transcript transcript we evaluate the variant against
     * @param sequence   FASTA sequence for the calculation
     * @return <code>1.</code> if variant creates a new <code>AG</code> dinucleotide within the <em>AG exclusion zone</em>
     * and <code>0.</code> otherwise. Note that {@link Double#NaN} is returned if <code>sequence</code> does not contain
     * sufficient nucleotide sequence
     */
    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);

        if (locationData.getAcceptorBoundary().isEmpty()) {
            // no acceptor boundary, the variant is located within the coding region or canonical donor region
            // of the first exon
            return 0.;
        }

        final GenomePosition acceptorBoundary = locationData.getAcceptorBoundary().get();
        final GenomeInterval agezInterval = new GenomeInterval(acceptorBoundary.shifted(agezBegin), -(agezBegin - agezEnd));
        if (!variant.getGenomeInterval().overlapsWith(agezInterval)) {
            // variant is not located within AG exclusion zone region
            return 0.;
        }

        // get alleles padded by 1 bp from each side
        final String refAllele = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 1);
        final String altAllele = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getAlt(), 1);
        if (refAllele == null || altAllele == null) {
            // unable to create padded alleles due to insufficient sequence. This should not happen since we fetch
            // the entire sequence of the transcript region +- padding
            return Double.NaN;
        }

        /*
        A new AG di-nucleotide is created if altAllele is a match while refAllele is not
         */
        final boolean refMatches = AG_PATTERN.matcher(refAllele).matches();
        final boolean altMatches = AG_PATTERN.matcher(altAllele).matches();

        return altMatches && !refMatches
                ? 1.
                : 0.;
    }
}
