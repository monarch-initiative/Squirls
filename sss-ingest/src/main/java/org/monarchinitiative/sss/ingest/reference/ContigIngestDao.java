package org.monarchinitiative.sss.ingest.reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 */
public class ContigIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContigIngestDao.class);

    private final DataSource dataSource;

    public ContigIngestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public int insertContig(String contig, int contigLength) {
        String sql = "insert into splicing.contigs(contig, contig_length) values (?, ?)";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, contig);
            statement.setInt(2, contigLength);
            return statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn("Error", e);
            return 0;
        }
    }

}
