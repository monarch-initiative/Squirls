package org.monarchinitiative.squirls.ingest.dao;

import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 */
public class PwmIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PwmIngestDao.class);

    private final DataSource dataSource;


    public PwmIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Insert PWM represented by {@link DoubleMatrix} into database.
     *
     * @param matrix {@link DoubleMatrix} that represents PWM
     * @param name   name of the PWM
     * @param exon   number of exonic positions spanned by <code>matrix</code>
     * @param intron number of intronic positions spanned by <code>matrix</code>
     * @return number of affected database rows
     */
    public int insertDoubleMatrix(DoubleMatrix matrix, String name, int exon, int intron) {
        int nRows = 0;
        String pwmDataSql = "INSERT INTO SPLICING.PWM_DATA(PWM_NAME, ROW_IDX, COL_IDX, CELL_VALUE) VALUES (?, ?, ?, ?)";
        String pwmMetadataSql = "INSERT INTO SPLICING.PWM_METADATA(PWM_NAME, PWM_KEY, PWM_VALUE) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement dataStatement = connection.prepareStatement(pwmDataSql);
                 PreparedStatement metadataStatement = connection.prepareStatement(pwmMetadataSql)) {

                // store PWM data
                dataStatement.setString(1, name);
                for (int rowIdx = 0; rowIdx < matrix.rows; rowIdx++) { // outer list, corresponds to 4 rows (4 nucleotides)
                    DoubleMatrix row = matrix.getRow(rowIdx);
                    for (int colIdx = 0; colIdx < row.length; colIdx++) { // inner list, corresponds to n positions of PWM
                        dataStatement.setInt(2, rowIdx);
                        dataStatement.setInt(3, colIdx);
                        dataStatement.setDouble(4, row.get(colIdx));
                        nRows += dataStatement.executeUpdate();
                    }
                }

                // store PWM metadata
                metadataStatement.setString(1, name);
                // number of exonic nucleotides spanned by the PWM
                metadataStatement.setString(2, "EXON");
                metadataStatement.setInt(3, exon);
                nRows += metadataStatement.executeUpdate();
                // number of intronic nucleotides spanned by the PWM
                metadataStatement.setString(2, "INTRON");
                metadataStatement.setInt(3, intron);
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
