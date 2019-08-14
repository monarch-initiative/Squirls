package org.monarchinitiative.threes.ingest.pwm;

import org.monarchinitiative.threes.core.calculators.ic.PositionWeightMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PwmIngestRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PwmIngestRunner.class);

    private final PwmIngestDao dao;

    private final Path filePath;

    public PwmIngestRunner(PwmIngestDao dao, Path filePath) {
        this.dao = dao;
        this.filePath = filePath;
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

    @Override
    public void run() {
        try (InputStream is = Files.newInputStream(filePath)) {
            Map<String, PositionWeightMatrix> stringPositionWeightMatrixMap = parseAll(is);
            for (Map.Entry<String, PositionWeightMatrix> entry : stringPositionWeightMatrixMap.entrySet()) {
                LOGGER.info("Inserting {} PWM", entry.getKey());
                dao.insertPositionWeightMatrix(entry.getValue());
            }
        } catch (IOException e) {
            LOGGER.warn("Error occured", e);
        }
    }
}
