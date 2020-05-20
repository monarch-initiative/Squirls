package org.monarchinitiative.threes.core.data.kmer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbKMerDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbKMerDao.class);

    private final DataSource dataSource;

    public DbKMerDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Map<String, Double> getScoreMap(String sql) {
        Map<String, Double> septamerMap = new HashMap<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sequence = rs.getString(1);
                    double score = rs.getDouble(2);
                    septamerMap.put(sequence, score);
                }
            }

        } catch (SQLException e) {
            LOGGER.warn("Error occurred during loading score map from database", e);
        }

        return septamerMap;
    }

    public Map<String, Double> getHexamerMap() {
        return getScoreMap("select SEQUENCE, SCORE from SPLICING.HEXAMERS");
    }

    public Map<String, Double> getSeptamerMap() {
        return getScoreMap("select SEQUENCE, SCORE from SPLICING.SEPTAMERS");
    }
}
