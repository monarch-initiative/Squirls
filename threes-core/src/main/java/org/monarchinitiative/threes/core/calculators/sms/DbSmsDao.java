package org.monarchinitiative.threes.core.calculators.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DbSmsDao implements SMSParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSmsDao.class);

    private final DataSource dataSource;

    public DbSmsDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Map<String, Double> getSeptamerMap() {
        Map<String, Double> septamerMap = new HashMap<>();

        String sql = "select SEQUENCE, SCORE from SPLICING.SEPTAMERS";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String sequence = rs.getString(1);
                double score = rs.getDouble(2);
                septamerMap.put(sequence, score);
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occured during loading septamer map from database", e);
        }

        return septamerMap;
    }
}
