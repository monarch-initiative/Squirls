package org.monarchinitiative.threes.ingest.septamers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class SeptamerIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeptamerIngestDao.class);

    private final DataSource dataSource;

    public SeptamerIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertSeptamers(Map<String, Double> septamerMap) {
        int nRows = 0;

        String septamerSql = "insert into SPLICING.SEPTAMERS(SEQUENCE, SCORE) values (?,?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(septamerSql)) {
            for (Map.Entry<String, Double> entry : septamerMap.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setDouble(2, entry.getValue());
                nRows += ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during PWM tables update", e);
        }

        return nRows;
    }
}
