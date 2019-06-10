package org.monarchinitiative.sss.core.data;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.sss.core.ThreeSRuntimeException;
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
public class ContigLengthDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContigLengthDao.class);

    private final DataSource dataSource;

    public ContigLengthDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Integer> getContigLengths() {
        String sql = "select CONTIG, CONTIG_LENGTH from SPLICING.CONTIGS";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            final ResultSet rs = ps.executeQuery();
            Map<String, Integer> contigLengthsMap = new HashMap<>();
            while (rs.next()) {
                final String contig = rs.getString("CONTIG");
                final int contig_length = rs.getInt("CONTIG_LENGTH");
                contigLengthsMap.put(contig, contig_length);
            }
            return ImmutableMap.copyOf(contigLengthsMap);
        } catch (SQLException e) {
            LOGGER.warn("Error occurred while retrieving contig lengths from db", e);
            throw new ThreeSRuntimeException(e);
        }
    }
}
