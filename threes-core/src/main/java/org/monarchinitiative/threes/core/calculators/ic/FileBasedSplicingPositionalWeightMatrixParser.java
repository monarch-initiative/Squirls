package org.monarchinitiative.threes.core.calculators.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.monarchinitiative.threes.core.calculators.ic.PwmUtils.mapToDoubleMatrix;


/**
 * Decode Yaml file with <em>position weight matrix</em> definitions into {@link PositionWeightMatrix} objects.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @see PositionWeightMatrix
 */
public class FileBasedSplicingPositionalWeightMatrixParser implements SplicingPositionalWeightMatrixParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedSplicingPositionalWeightMatrixParser.class);

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
    public FileBasedSplicingPositionalWeightMatrixParser(InputStream is) {
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

    @Override
    public SplicingParameters getSplicingParameters() {
        return splicingParameters;
    }

    @Override
    public DoubleMatrix getDonorMatrix() {
        return donorMatrix;
    }


    @Override
    public DoubleMatrix getAcceptorMatrix() {
        return acceptorMatrix;
    }

}
