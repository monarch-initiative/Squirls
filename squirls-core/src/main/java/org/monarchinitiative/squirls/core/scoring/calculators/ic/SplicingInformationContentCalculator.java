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
