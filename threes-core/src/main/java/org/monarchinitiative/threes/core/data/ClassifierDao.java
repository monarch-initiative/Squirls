package org.monarchinitiative.threes.core.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClassifierDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassifierDao.class);

    private final DataSource dataSource;

    public ClassifierDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    /**
     * Returns bytes that correspond to classifier with a given {@code version}.
     *
     * @param version version string, e.g. `v1.0.0`
     * @return byte array that corresponds to the classifier or {@code null} if classifier with the {@code version}
     * does not exist in the database
     */
    public byte[] readClassifier(String version) {
        String sql = "select data from SPLICING.CLASSIFIER where version = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.first()) {
                    return rs.getBytes("data");
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return null;
    }
}
