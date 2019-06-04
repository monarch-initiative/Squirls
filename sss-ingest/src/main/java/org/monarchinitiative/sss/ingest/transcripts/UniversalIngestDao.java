package org.monarchinitiative.sss.ingest.transcripts;

import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 *
 */
public class UniversalIngestDao implements IngestDao {

    private final JdbcTemplate template;

    public UniversalIngestDao(JdbcTemplate template) {
        this.template = template;
    }

    public UniversalIngestDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }


    @Override
    public int insertTranscript(SplicingTranscript transcript) {
        return 0;
    }
}
