package org.monarchinitiative.threes.core.data;

import org.apache.commons.io.IOUtils;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.io.Deserializer;
import org.monarchinitiative.threes.core.classifier.transform.prediction.IdentityTransformer;
import org.monarchinitiative.threes.core.classifier.transform.prediction.LogisticRegressionPredictionTransformer;
import org.monarchinitiative.threes.core.classifier.transform.prediction.PredictionTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This dao handles business regarding classifier and prediction process.
 */
public class DbClassifierDataManager implements ClassifierDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbClassifierDataManager.class);

    private static final Pattern DEJSONIFY_PATTERN = Pattern.compile("\"(?<key>\\w+)\":\\s*(?<value>-?\\d+\\.\\d*)");

    private final DataSource dataSource;

    public DbClassifierDataManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Turn parameter map into a JSON-like string.
     *
     * @param parameters map with parameters
     * @return JSON-like string
     */
    static String jsonify(Map<String, Double> parameters) {
        return parameters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("\"%s\": %.12f", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Turn a JSON string containing <em>String -> Double</em> mappings to a corresponding Map.
     *
     * @param payload to de-jsonify
     * @return Map with decoded values
     */
    static Map<String, Double> deJsonify(String payload) {
        Map<String, Double> map = new HashMap<>();
        final Matcher matcher = DEJSONIFY_PATTERN.matcher(payload);
        while (matcher.find()) {
            map.put(matcher.group("key"), Double.parseDouble(matcher.group("value")));
        }

        return map;
    }

    private static String readAll(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);
        return writer.toString();
    }

    /**
     * @return collection versions of available classifiers
     */
    public Collection<String> getAvailableClassifiers() {
        String sql = "select version from SPLICING.CLASSIFIER";
        final Set<String> versions = new HashSet<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    versions.add(rs.getString("version"));
            }

        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return versions;
    }

    public int storeClassifier(String version, byte[] clfBytes) {
        int updated = 0;
        String sql = "insert into SPLICING.CLASSIFIER(version, data) values (?, ?)";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, version);
            ps.setBytes(2, clfBytes);
            updated += ps.executeUpdate();
            return updated;
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
            return 0;
        }
    }

    public Optional<byte[]> readClassifierBytes(String version) {
        String sql = "select data from SPLICING.CLASSIFIER where version = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.first()) {
                    return Optional.of(rs.getBytes("data"));
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return Optional.empty();
    }

    /**
     * Returns classifier optional corresponding to a given {@code version}.
     *
     * @param version version string, e.g. `v1.0.0`
     * @return classifier optional
     */
    public Optional<OverlordClassifier> readClassifier(String version) {
        return readClassifierBytes(version).map(bytes -> Deserializer.deserialize(new ByteArrayInputStream(bytes)));
    }

    @Override
    public int storeTransformer(final String version, final PredictionTransformer transformer) {
        int updated = 0;

        final String type;
        final Map<String, Double> parameters;
        if (transformer instanceof LogisticRegressionPredictionTransformer) {
            // we store slope and intercept parameters here
            LogisticRegressionPredictionTransformer transfm = (LogisticRegressionPredictionTransformer) transformer;
            type = "logreg";
            parameters = Map.of("slope", transfm.getSlope(), "intercept", transfm.getIntercept());
        } else {
            // no parameters to store
            type = "identity";
            parameters = Map.of();
        }

        final String json = jsonify(parameters);

        String sql = "insert into SPLICING.CLASSIFIER_METADATA(version, clf_type, metadata) values ( ?, ?, ? )";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, version);
            ps.setString(2, type);
            ps.setClob(3, new StringReader(json));
            updated += ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return updated;
    }

    @Override
    public Optional<PredictionTransformer> readTransformer(String version) {
        String sql = "select clf_type, metadata from SPLICING.CLASSIFIER_METADATA where version = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, version);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // get parameters
                    final String clfType = rs.getString("clf_type");
                    final Clob metadata = rs.getClob("metadata");
                    final String payload = readAll(metadata.getAsciiStream());
                    final Map<String, Double> parameters = deJsonify(payload);

                    // decide which transformer to create and which parameters to use
                    final PredictionTransformer transformer;
                    switch (clfType.toLowerCase()) {
                        case "logreg":
                            LOGGER.debug("Using log reg prediction transformer");
                            transformer = LogisticRegressionPredictionTransformer.getInstance(parameters.get("slope"), parameters.get("intercept"));
                            break;
                        case "identity":
                        default:
                            LOGGER.debug("Using identity/no-op transformer");
                            transformer = IdentityTransformer.getInstance();
                            break;
                    }
                    return Optional.of(transformer);
                }
            }
        } catch (SQLException | IOException e) {
            LOGGER.warn("Error: ", e);
        }
        return Optional.empty();
    }
}
