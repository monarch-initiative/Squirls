package org.monarchinitiative.squirls.ingest.conservation;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class BigWigIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigWigIngestDao.class);

    private final DataSource dataSource;

    public BigWigIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Convert list of floats into a byte array. Each float occupies 4 bytes of space in the array.
     *
     * @param values list of floats
     * @return byte array with length of multiple of 4 where each 4 bytes represent a float from the input {@code values}
     */
    private static byte[] encodeFloatsToBytes(float[] values) {
        byte[] arr = new byte[values.length * 4];
        for (int i = 0; i < values.length; i++) {
            // convert float into 4 bytes
            byte[] current = ByteBuffer.allocate(4).putFloat(values[i]).array();
            // put the bytes into the array
            System.arraycopy(current, 0, arr, i * 4, 4);
        }
        return arr;
    }

    public int insertScores(String symbol, GenomeInterval interval, float[] values) {
        int updatedRows = 0;

        try (final Connection connection = dataSource.getConnection()) {
            //    SYMBOL           char(50) not null,
            //    CONTIG           int      not null,  -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
            //    BEGIN_POS        int      not null,  -- 0-based (exclusive) begin position of the region on STRAND
            //    END_POS          int      not null,  -- 0-based (inclusive) end position of the region on STRAND
            //    STRAND           bool     not null,  -- true if FWD, false if REV
            //    PHYLOP_VALUES    blob     not null   -- PHYLOP values as bytes

            // store indices & names
            String refSeqSql = "INSERT INTO SPLICING.PHYLOP_SCORE " +
                    "(SYMBOL, CONTIG, BEGIN_POS, END_POS, STRAND, PHYLOP_VALUES) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (final PreparedStatement ps = connection.prepareStatement(refSeqSql)) {
                ps.setString(1, symbol);
                ps.setInt(2, interval.getChr());
                ps.setInt(3, interval.getBeginPos());
                ps.setInt(4, interval.getEndPos());
                ps.setBoolean(5, interval.getStrand().isForward());
                ps.setBytes(6, encodeFloatsToBytes(values));
                updatedRows += ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return updatedRows;
    }
}
