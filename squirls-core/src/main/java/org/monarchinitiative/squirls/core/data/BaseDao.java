package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class provides reference dictionary that represents coordinate system of data stored in SQUIRLS database.
 */
public abstract class BaseDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);

    /**
     * This reference dictionary maps contig ids from database to contig names. The internal reference dictionary should
     * be used for mapping contig id from database into chromosome name.
     */
    protected final ReferenceDictionary internalReferenceDictionary;

    protected final DataSource dataSource;

    protected BaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.internalReferenceDictionary = readReferenceDictionary(dataSource);
    }

    public static ReferenceDictionary readReferenceDictionary(DataSource dataSource) {
        final ReferenceDictionaryBuilder builder = new ReferenceDictionaryBuilder();

        try (Connection connection = dataSource.getConnection()) {
            // load indices & names
            String idNameSql = "SELECT ID, NAME FROM SPLICING.REF_DICT_ID_NAME";
            try (final PreparedStatement idNamePs = connection.prepareStatement(idNameSql);
                 final ResultSet idNameRs = idNamePs.executeQuery()) {
                while (idNameRs.next()) {
                    builder.putContigName(idNameRs.getInt("ID"), idNameRs.getString("NAME"));
                }
            } catch (SQLException e) {
                LOGGER.warn("Error: ", e);
                return new ReferenceDictionaryBuilder().build();
            }

            // load indices & lengths
            String idLengthSql = "SELECT ID, LENGTH FROM SPLICING.REF_DICT_ID_LENGTH";
            try (final PreparedStatement idNamePs = connection.prepareStatement(idLengthSql);
                 final ResultSet idNameRs = idNamePs.executeQuery()) {
                while (idNameRs.next()) {
                    builder.putContigLength(idNameRs.getInt("ID"), idNameRs.getInt("LENGTH"));
                }
            } catch (SQLException e) {
                LOGGER.warn("Error: ", e);
                return new ReferenceDictionaryBuilder().build();
            }

            // load indices & names
            String nameIdSql = "SELECT ID , NAME as name FROM SPLICING.REF_DICT_NAME_ID";
            try (final PreparedStatement nameIdPs = connection.prepareStatement(nameIdSql);
                 final ResultSet nameIdRs = nameIdPs.executeQuery()) {
                while (nameIdRs.next()) {
                    builder.putContigID(nameIdRs.getString("NAME"), nameIdRs.getInt("ID"));
                }
            } catch (SQLException e) {
                LOGGER.warn("Error: ", e);
                return new ReferenceDictionaryBuilder().build();
            }

        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
            return new ReferenceDictionaryBuilder().build();
        }


        return builder.build();
    }
}
