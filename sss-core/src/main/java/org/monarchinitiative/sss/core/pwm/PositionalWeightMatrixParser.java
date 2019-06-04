package org.monarchinitiative.sss.core.pwm;

import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
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
public class PositionalWeightMatrixParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionalWeightMatrixParser.class);

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
     * @throws IOException if error occurs during Yaml decoding
     */
    public PositionalWeightMatrixParser(InputStream is) throws IOException {
        Map<String, PositionWeightMatrix> matrixMap = parseAll(is);
        this.donorMatrix = mapToDoubleMatrix(matrixMap.get(DONOR_M_NAME));
        this.acceptorMatrix = mapToDoubleMatrix(matrixMap.get(ACCEPTOR_M_NAME));
        this.splicingParameters = SplicingParameters.builder()
                .setDonorExonic(matrixMap.get(DONOR_M_NAME).getExon())
                .setDonorIntronic(matrixMap.get(DONOR_M_NAME).getIntron())
                .setAcceptorExonic(matrixMap.get(ACCEPTOR_M_NAME).getExon())
                .setAcceptorIntronic(matrixMap.get(ACCEPTOR_M_NAME).getIntron())
                .build();
    }
//    private final int donorExon, donorIntron, acceptorExon, acceptorIntron;

    /**
     * Map {@link PositionWeightMatrix} to {@link DoubleMatrix} and perform sanity checks:
     * <ul>
     * <li>entries for all 4 nucleotides must be present</li>
     * <li>entries for all nucleotides must have the same size</li>
     * <li>probabilities/frequencies at each position must sum up to 1</li>
     * </ul>
     *
     * @param pwm {@link PositionWeightMatrix} to be converted
     * @return {@link DoubleMatrix} with data from <code>pwm</code>
     */
    private static DoubleMatrix mapToDoubleMatrix(PositionWeightMatrix pwm) {
        // This list should contain another four lists. Each inner list represents one of the nucleotides A, C, G, T in this order.
        List<List<Double>> vals = pwm.getMatrix();
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
            if (Math.abs(sum - 1D) > EPSILON)
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

    /**
     * Decode records from provided Yaml file into corresponding {@link PositionWeightMatrix} objects, store them in a {@link Map}
     * by their names.
     *
     * @param is {@link InputStream} with PWM definitions in Yaml format as described in {@link PositionWeightMatrix} class
     *           description
     * @return {@link Map} key - PWM name, Value - {@link PositionWeightMatrix} object.
     * @throws IOException if there is problem reading file.
     */
    private static Map<String, PositionWeightMatrix> parseAll(InputStream is) throws IOException {
        Map<String, PositionWeightMatrix> matrixMap = new HashMap<>();

        Yaml yaml = new Yaml(new Constructor(PositionWeightMatrix.class));
        for (Object object : yaml.loadAll(is)) {
            PositionWeightMatrix matrix = (PositionWeightMatrix) object;

            String name = matrix.getName();
            matrixMap.put(name, matrix);
        }
        return matrixMap;
    }

    public SplicingParameters getSplicingParameters() {
        return splicingParameters;
    }

    public DoubleMatrix getDonorMatrix() {
        return donorMatrix;
    }


    public DoubleMatrix getAcceptorMatrix() {
        return acceptorMatrix;
    }

}
