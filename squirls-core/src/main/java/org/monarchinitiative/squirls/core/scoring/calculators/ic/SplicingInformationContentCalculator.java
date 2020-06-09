package org.monarchinitiative.squirls.core.scoring.calculators.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * This annotator implements splice site scoring method described in publication <a
 * href="https://www.ncbi.nlm.nih.gov/pubmed/9711873">Information Analysis of Human Splice Site Mutations</a> by Rogan
 * et al.<p></p>
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.0.1
 * @since 0.0
 */
public class SplicingInformationContentCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplicingInformationContentCalculator.class);

    /**
     * Nucleotide sequence submitted for scoring must match this pattern.
     */
    private static final Pattern PATTERN = Pattern.compile("[ACGTacgt]*");

    /**
     * Double matrices for splice donor and acceptor sites. Columns represent positions and rows represent probabilities
     * of presence of the nucleotides (A, C, G, T) at the position.
     */
    private final DoubleMatrix donorMatrix, acceptorMatrix;

    private final SplicingParameters splicingParameters;

    /**
     * Instantiate the annotator. Perform sanity check of provided PWM definitions.
     *
     * @param donorMatrix    - matrix of nucleotide frequencies observed on splice donor sites genome-wise
     * @param acceptorMatrix - matrix of nucleotide frequencies observed on splice acceptor sites genome-wise
     */
    public SplicingInformationContentCalculator(DoubleMatrix donorMatrix, DoubleMatrix acceptorMatrix, SplicingParameters splicingParameters) {
        this.donorMatrix = createICMatrix(donorMatrix);
        this.acceptorMatrix = createICMatrix(acceptorMatrix);
        this.splicingParameters = splicingParameters;
    }

    public SplicingInformationContentCalculator(SplicingPwmData splicingPwmData) {
        this(splicingPwmData.getDonor(), splicingPwmData.getAcceptor(), splicingPwmData.getParameters());
    }

    /**
     * Convert {@link DoubleMatrix} with nucleotide frequencies into array of information content values.
     * <p>
     * Basically, {@link #calculateIC(double)} method is mapped element-wise to given <code>freqMatrix</code>.
     *
     * @param freqMatrix {@link DoubleMatrix} containing nucleotide frequencies at positions of splice site
     * @return {@link DoubleMatrix} containing information content of nucleotides at positions of splice site with the
     * same shape as <code>freqMatrix</code>
     */
    private static DoubleMatrix createICMatrix(DoubleMatrix freqMatrix) {
        DoubleMatrix icm = new DoubleMatrix(freqMatrix.rows, freqMatrix.columns);

        for (int i = 0; i < freqMatrix.rows; i++) { // iterate through positions/rows of io
            DoubleMatrix row = freqMatrix.getRow(i);
            for (int j = 0; j < row.columns; j++) { // iterate through nucleotides at position
                icm.put(i, j, calculateIC(row.get(j)));
            }
        }
        return icm;
    }

    /**
     * Calculate information content of the nucleotide from the frequency using formula 1 (Rogan paper from class
     * description). Correction factor is ignored, I assume that the sample size used to calculate the nucleotide
     * frequency is large enough. In case of the splice sites it was ~220000 sites.
     *
     * @param freq {@link Double} frequency of nucleotide occurence at its position from range <0, 1>
     * @return {@link Double} with information content value
     */
    private static double calculateIC(double freq) {
        return 2d - (-Math.log(freq) / Math.log(2));
    }

    /**
     * Convert sequence into its binary mask representation. For sequence 'ACGTAT' the corresponding binary mask
     * is:
     *
     * <pre>
     *    |  0    1    2    3    4    5
     * ---|------------------------------
     *  A |  1    0    0    0    1    0
     *  C |  0    1    0    0    0    0
     *  G |  0    0    1    0    0    0
     *  T |  0    0    0    1    0    1
     * </pre>
     *
     * @param sequence String with nucleotides represented by [ACGTacgt] characters.
     * @return binary mask {@link DoubleMatrix} with <code>i</code> rows and <code>j</code> columns. <code>i</code> =
     * 4 (nucleotides) and <code>j</code> = length of the <code>sequence</code>. Presence of the nucleotide at given position is
     * represented by 1d, absence is represented by 0D
     */
    private static DoubleMatrix binaryMask(String sequence) {
        DoubleMatrix binary_mask = new DoubleMatrix(4, sequence.length());
        char[] nts = sequence.toCharArray();
        for (int i = 0; i < nts.length; i++) {
            switch (nts[i]) {
                case 'A':
                case 'a':
                    binary_mask.put(0, i, 1D);
                    break;

                case 'C':
                case 'c':
                    binary_mask.put(1, i, 1D);
                    break;

                case 'G':
                case 'g':
                    binary_mask.put(2, i, 1D);
                    break;

                case 'T':
                case 't':
                    binary_mask.put(3, i, 1D);
                    break;
                default:
                    break; // no special action, we check sequence integrity upstream.
            }
        }
        return binary_mask;
    }

    public SplicingParameters getSplicingParameters() {
        return splicingParameters;
    }

    /**
     * @param sequence String with nucleotide sequence to be scored
     * @return score of the <code>sequence</code> as if it was splice donor site calculated using <em>information content</em>
     * method or {@link Double#NaN} if the length of <code>sequence</code> is not equal to length of splice donor site
     */
    public double getSpliceDonorScore(String sequence) {
        if (sequence.length() != this.donorMatrix.columns) {
            LOGGER.debug(String.format("Unable to calculate donor score for sequence '%s'. Length of sequence: %d, length " +
                    "of donor matrix: %d", sequence, sequence.length(), this.donorMatrix.columns));
            return Double.NaN;
        } else if (!PATTERN.matcher(sequence).matches()) {
            LOGGER.debug(String.format("Unable to calculate donor score for sequence '%s'. Only characters A,C,G,T and a," +
                    "c,g,t are allowed.", sequence));
            return Double.NaN;
        }
        return donorMatrix.mul(binaryMask(sequence)).sum();
    }


    /**
     * @param sequence String with nucleotide sequence to be scored
     * @return score of the sequence as if it was splice acceptor site calculated using <em>information content</em>
     * method or {@link Double#NaN} if the length of <code>sequence</code> is not equal to length of splice acceptor site
     */
    public double getSpliceAcceptorScore(String sequence) {
        if (sequence.length() != this.acceptorMatrix.columns) {
            LOGGER.debug(String.format("Unable to calculate acceptor score for sequence '%s'. Length of sequence: %d, " +
                    "length of acceptor matrix: %d", sequence, sequence.length(), this.acceptorMatrix.columns));
            return Double.NaN;
        } else if (!PATTERN.matcher(sequence).matches()) {
            LOGGER.debug(String.format("Unable to calculate acceptor score for sequence '%s'. Only characters A,C,G,T " +
                    "and a,c,g,t are " +
                    "allowed.", sequence));
            return Double.NaN;
        }
        return acceptorMatrix.mul(binaryMask(sequence)).sum();
    }
}
