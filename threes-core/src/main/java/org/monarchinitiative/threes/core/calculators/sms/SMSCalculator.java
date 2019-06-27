package org.monarchinitiative.threes.core.calculators.sms;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.threes.core.Utils;

import java.util.Map;

/**
 * This class calculates <em>SMS score</em> for given nucleotide sequence.
 * <p>
 * <h2>SMS score</h2>
 * The score is described in <a href="https://www.ncbi.nlm.nih.gov/pubmed/29242188">
 * Saturation mutagenesis reveals manifold determinants of exon definition</a> paper.
 * <p>
 * <h2>Calculation</h2>
 * It is possible to calculate the score for any nucleotide sequence longer than 6bp. For sequences longer than 7bp, the
 * score is a sum of individual 7-bp window scores.
 */
public class SMSCalculator {

    private final ImmutableMap<String, Double> septamerMap;

    public SMSCalculator(Map<String, Double> septamerMap) {
        this.septamerMap = ImmutableMap.copyOf(septamerMap);
    }

    /**
     * Calculate SMS score for given nucleotide sequence.
     *
     * @param sequence String with sequence to be scored
     * @return SMS score for the sequence or {@link Double#NaN} if there is invalid nucleotide character present, or if
     * length of the {@code sequence} is less than 7
     */
    public double scoreSequence(String sequence) {
        return Utils.slidingWindow(sequence.toUpperCase(), 7)
                .map(septamer -> septamerMap.getOrDefault(septamer, Double.NaN))
                .reduce(Double::sum)
                .orElse(Double.NaN);
    }
}
