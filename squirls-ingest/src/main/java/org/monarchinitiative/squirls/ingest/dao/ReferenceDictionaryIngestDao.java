package org.monarchinitiative.squirls.ingest.dao;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
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
public class ReferenceDictionaryIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDictionaryIngestDao.class);

    private final DataSource dataSource;

    public ReferenceDictionaryIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @param referenceDictionary {@link ReferenceDictionary} with entries prefixed by <code>chr</code>
     */
    public void saveReferenceDictionary(ReferenceDictionary referenceDictionary) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // store indices & names
            String idNameSql = "INSERT INTO SPLICING.REF_DICT_ID_NAME VALUES (?, ?)";
            try (final PreparedStatement idNamePs = connection.prepareStatement(idNameSql)) {
                int updatedIdName = 0;
                for (Map.Entry<Integer, String> entry : referenceDictionary.getContigIDToName().entrySet()) {
                    idNamePs.setInt(1, entry.getKey());
                    idNamePs.setString(2, entry.getValue());
                    updatedIdName += idNamePs.executeUpdate();
                }
                LOGGER.debug("Updated '{}' records in the SPLICING.REF_DICT_ID_NAME table", updatedIdName);
            } catch (SQLException e) {
                connection.rollback();
                LOGGER.warn("Error: ", e);
                return;
            }

            // store indices & lengths
            String idLengthSql = "INSERT INTO SPLICING.REF_DICT_ID_LENGTH (ID, LENGTH) VALUES (?, ?)";
            try (final PreparedStatement idLengthPs = connection.prepareStatement(idLengthSql)) {
                int updatedIdLength = 0;
                for (Map.Entry<Integer, Integer> entry : referenceDictionary.getContigIDToLength().entrySet()) {
                    idLengthPs.setInt(1, entry.getKey());
                    idLengthPs.setInt(2, entry.getValue());
                    updatedIdLength += idLengthPs.executeUpdate();
                }
                LOGGER.debug("Updated '{}' records in the SPLICING.REF_DICT_ID_LENGTH table", updatedIdLength);
            } catch (SQLException e) {
                connection.rollback();
                LOGGER.warn("Error: ", e);
                return;
            }

            // store names & indices
            String nameIdSql = "INSERT INTO SPLICING.REF_DICT_NAME_ID (NAME, ID) VALUES (?, ?)";
            try (final PreparedStatement nameIdPs = connection.prepareStatement(nameIdSql)) {
                int updatedIdName = 0;
                for (Map.Entry<String, Integer> entry : referenceDictionary.getContigNameToID().entrySet()) {
                    nameIdPs.setString(1, entry.getKey());
                    nameIdPs.setInt(2, entry.getValue());
                    updatedIdName += nameIdPs.executeUpdate();
                }
                LOGGER.debug("Updated '{}' records in the SPLICING.REF_DICT_NAME_ID table", updatedIdName);
            } catch (SQLException e) {
                connection.rollback();
                LOGGER.warn("Error: ", e);
                return;
            }
            connection.commit();
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }

    }

}
