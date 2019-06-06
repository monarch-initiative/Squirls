package org.monarchinitiative.sss.ingest.pwm;

import org.monarchinitiative.sss.core.pwm.PositionWeightMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class PwmIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PwmIngestDao.class);

    private final DataSource dataSource;


    public PwmIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertPositionWeightMatrix(PositionWeightMatrix posWeightMatrix) {
        int nRows = 0;
        String pwmDataSql = "INSERT INTO SPLICING.PWM_DATA(PWM_NAME, ROW_IDX, COL_IDX, CELL_VALUE) VALUES (?, ?, ?, ?)";
        String pwmMetadataSql = "INSERT INTO SPLICING.PWM_METADATA(PWM_NAME, PWM_KEY, PWM_VALUE) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement dataStatement = connection.prepareStatement(pwmDataSql);
                 PreparedStatement metadataStatement = connection.prepareStatement(pwmMetadataSql)) {

                // store PWM data
                List<List<Double>> matrix = posWeightMatrix.getMatrix();
                dataStatement.setString(1, posWeightMatrix.getName());
                for (int colIdx = 0; colIdx < matrix.size(); colIdx++) {
                    List<Double> row = matrix.get(colIdx);
                    for (int rowIdx = 0; rowIdx < row.size(); rowIdx++) {
                        dataStatement.setInt(2, rowIdx);
                        dataStatement.setInt(3, colIdx);
                        dataStatement.setDouble(4, row.get(rowIdx));
                        nRows += dataStatement.executeUpdate();
                    }
                }

                // store PWM metadata
                metadataStatement.setString(1, posWeightMatrix.getName());
                // number of exonic nucleotides spanned by the PWM
                metadataStatement.setString(2, "EXON");
                metadataStatement.setInt(3, posWeightMatrix.getExon());
                nRows += metadataStatement.executeUpdate();
                // number of intronic nucleotides spanned by the PWM
                metadataStatement.setString(2, "INTRON");
                metadataStatement.setInt(3, posWeightMatrix.getIntron());
                nRows += metadataStatement.executeUpdate();

            } catch (SQLException e) {
                connection.rollback();
                LOGGER.warn("Error occurred during PWM tables update, rolling back changes", e);
            }


            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.warn("Error occurred during PWM tables update", e);
        }
        return nRows;
    }
}
