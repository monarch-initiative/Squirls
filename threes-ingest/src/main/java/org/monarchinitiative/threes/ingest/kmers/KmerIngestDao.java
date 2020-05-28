package org.monarchinitiative.threes.ingest.kmers;

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
public class KmerIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(KmerIngestDao.class);

    private final DataSource dataSource;

    public KmerIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertSeptamers(Map<String, Double> septamerMap) {
        String septamerSql = "insert into SPLICING.SEPTAMERS(SEQUENCE, SCORE) values (?,?)";
        return insertKmerMap(septamerMap, septamerSql);
    }


    public int insertHexamers(Map<String, Double> hexamerMap) {
        String hexamerSql = "insert into SPLICING.HEXAMERS(SEQUENCE, SCORE) values (?,?)";
        return insertKmerMap(hexamerMap, hexamerSql);
    }

    private int insertKmerMap(Map<String, Double> hexamerMap, String hexamerSql) {
        int nRows = 0;
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(hexamerSql)) {
                for (Map.Entry<String, Double> entry : hexamerMap.entrySet()) {
                    ps.setString(1, entry.getKey());
                    ps.setDouble(2, entry.getValue());
                    nRows += ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during PWM tables update", e);
        }

        return nRows;
    }
}
