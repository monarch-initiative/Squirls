package org.monarchinitiative.threes.core.data.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Decode Yaml file with <em>position weight matrix</em> definitions into {@link PositionWeightMatrix} objects.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @see PositionWeightMatrix
 */
public class InputStreamBasedPositionalWeightMatrixParser implements SplicingPositionalWeightMatrixParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamBasedPositionalWeightMatrixParser.class);

    private static final String DONOR_M_NAME = "SPLICE_DONOR_SITE";

    private static final String ACCEPTOR_M_NAME = "SPLICE_ACCEPTOR_SITE";

    /**
     * Tolerance when checking that probabilities sum up to 1.
     */
    private static final double EPSILON = 0.004;

    private final DoubleMatrix donorMatrix;

    private final DoubleMatrix acceptorMatrix;

    private final SplicingParameters splicingParameters;

    /**
     * Parse Yaml content of provided input stream, create PWM representations ({@link PositionWeightMatrix}) and store
     * in a {@link Map} by their names.
     *
     * @param is {@link InputStream} with PWM definitions in Yaml format as described in {@link PositionWeightMatrix} class
     *           description
     */
    public InputStreamBasedPositionalWeightMatrixParser(InputStream is) {
        Map<String, PositionWeightMatrix> matrixMap = parseAll(is);
        this.donorMatrix = mapToDoubleMatrix(matrixMap.get(DONOR_M_NAME).getMatrix(), EPSILON);
        this.acceptorMatrix = mapToDoubleMatrix(matrixMap.get(ACCEPTOR_M_NAME).getMatrix(), EPSILON);
        this.splicingParameters = SplicingParameters.builder()
                .setDonorExonic(matrixMap.get(DONOR_M_NAME).getExon())
                .setDonorIntronic(matrixMap.get(DONOR_M_NAME).getIntron())
                .setAcceptorExonic(matrixMap.get(ACCEPTOR_M_NAME).getExon())
                .setAcceptorIntronic(matrixMap.get(ACCEPTOR_M_NAME).getIntron())
                .build();
    }


    /**
     * Decode records from provided Yaml file into corresponding {@link PositionWeightMatrix} objects, store them in a {@link Map}
     * by their names.
     *
     * @param is {@link InputStream} with PWM definitions in Yaml format as described in {@link PositionWeightMatrix} class
     *           description
     * @return {@link Map} key - PWM name, Value - {@link PositionWeightMatrix} object.
     */
    private static Map<String, PositionWeightMatrix> parseAll(InputStream is) {
        Map<String, PositionWeightMatrix> matrixMap = new HashMap<>();

        Yaml yaml = new Yaml(new Constructor(PositionWeightMatrix.class));
        for (Object object : yaml.loadAll(is)) {
            PositionWeightMatrix matrix = (PositionWeightMatrix) object;

            String name = matrix.getName();
            matrixMap.put(name, matrix);
        }
        return matrixMap;
    }

    /**
     * Map {@link PositionWeightMatrix} to {@link DoubleMatrix} and perform sanity checks:
     * <ul>
     * <li>entries for all 4 nucleotides must be present</li>
     * <li>entries for all nucleotides must have the same size</li>
     * <li>probabilities/frequencies at each position must sum up to 1</li>
     * </ul>
     *
     * @param vals    This list should contain another four lists. Each inner list represents one of the nucleotides
     *                A, C, G, T in this order
     * @param epsilon Tolerance when checking that probabilities sum up to 1
     * @return {@link DoubleMatrix} with data from <code>io</code>
     */
    static DoubleMatrix mapToDoubleMatrix(List<List<Double>> vals, double epsilon) {

        if (vals == null)
            throw new IllegalArgumentException("Unable to create matrix with 0 rows");

        if (vals.size() != 4)
            throw new IllegalArgumentException("Matrix does not have 4 rows for 4 nucleotides");

        // all four lists must have the same size
        int size = vals.get(0).size();
        if (vals.stream().anyMatch(inner -> inner.size() != size))
            throw new IllegalArgumentException("Rows of the matrix do not have the same size");

        // probabilities at each position of donor and acceptor matrices sum up to 1 and issue a warning when
        // the difference is larger than allowed in the EPSILON} parameter
        for (int pos_idx = 0; pos_idx < size; pos_idx++) {
            double sum = 0;
            for (int nt_idx = 0; nt_idx < 4; nt_idx++) {
                sum += vals.get(nt_idx).get(pos_idx);
            }
            if (Math.abs(sum - 1D) > epsilon)
                throw new IllegalArgumentException(String.format("Probabilities do not sum up to 1 at column %d", pos_idx));
        }

        // checks are done
        DoubleMatrix dm = new DoubleMatrix(vals.size(), vals.get(0).size());
        for (int rowIdx = 0; rowIdx < vals.size(); rowIdx++) {
            List<Double> row = vals.get(rowIdx);
            for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                dm.put(rowIdx, colIdx, row.get(colIdx));
            }
        }
        return dm;

    }

    @Override
    public SplicingPwmData getSplicingPwmData() {
        return SplicingPwmData.builder()
                .setDonor(donorMatrix)
                .setAcceptor(acceptorMatrix)
                .setParameters(splicingParameters)
                .build();
    }

    /**
     * This POJO represents a position-weight matrix (PWM). The PWM attributes are:
     * <ul>
     * <li><b>name</b> - name of the PWM</li>
     * <li><b>matrix</b> - internal representation of PWM values used for scoring of nucleotide sequences</li>
     * </ul>
     */
    public static class PositionWeightMatrix {


        private String name;

        private List<List<Double>> matrix;

        private int exon;

        private int intron;


        public PositionWeightMatrix() {
        }

        public int getExon() {
            return exon;
        }

        public void setExon(int exon) {
            this.exon = exon;
        }

        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public List<List<Double>> getMatrix() {
            return matrix;
        }


        public void setMatrix(List<List<Double>> matrix) {
            this.matrix = matrix;
        }

        public int getIntron() {
            return intron;
        }

        public void setIntron(int intron) {
            this.intron = intron;
        }
    }
}
