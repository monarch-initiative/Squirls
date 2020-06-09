package org.monarchinitiative.squirls.core.data.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
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
        this.donorMatrix = Utils.mapToDoubleMatrix(matrixMap.get(DONOR_M_NAME).getMatrix(), EPSILON);
        this.acceptorMatrix = Utils.mapToDoubleMatrix(matrixMap.get(ACCEPTOR_M_NAME).getMatrix(), EPSILON);
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
