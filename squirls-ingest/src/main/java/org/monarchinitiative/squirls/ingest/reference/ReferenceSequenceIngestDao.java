package org.monarchinitiative.squirls.ingest.reference;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReferenceSequenceIngestDao {

    /**
     * Charset used to encode/decode fasta sequence into byte[].
     */
    static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceSequenceIngestDao.class);
    private final DataSource dataSource;

    public ReferenceSequenceIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertSequence(String symbol, SequenceInterval sequence) {
        int updatedRows = 0;
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            // SYMBOL char(50) not null,
            //    CONTIG       int      not null,-- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
            //    BEGIN_POS    int      not null,-- 0-based (exclusive) begin position of the region on STRAND
            //    END_POS      int      not null,-- 0-based (inclusive) end position of the region on STRAND
            //    STRAND       bool     not null,-- true if FWD, false if REV
            //    SEQUENCE    blob

            // store indices & names
            String refSeqSql = "INSERT INTO SPLICING.REF_SEQUENCE " +
                    "(SYMBOL, CONTIG, BEGIN_POS, END_POS, STRAND, FASTA_SEQUENCE) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (final PreparedStatement ps = connection.prepareStatement(refSeqSql)) {
                final GenomeInterval interval = sequence.getInterval();
                ps.setString(1, symbol);
                ps.setInt(2, interval.getChr());
                ps.setInt(3, interval.getBeginPos());
                ps.setInt(4, interval.getEndPos());
                ps.setBoolean(5, interval.getStrand().isForward());
                ps.setBytes(6, sequence.getSequence().getBytes(CHARSET));
                updatedRows += ps.executeUpdate();
                LOGGER.debug("Updated '{}' records in the SPLICING.REF_SEQUENCE table", updatedRows);

            } catch (SQLException e) {
                connection.rollback();
                LOGGER.warn("Error: ", e);
            }
            connection.commit();
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return updatedRows;
    }
}
