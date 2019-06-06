package org.monarchinitiative.sss.core.data;

import org.monarchinitiative.sss.core.model.SplicingTranscript;

import javax.sql.DataSource;
import java.util.List;

/**
 *
 */
public class SplicingDataSourceImpl implements SplicingDataSource {

    private final DataSource dataSource;

    public SplicingDataSourceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<SplicingTranscript> fetchTranscripts(String contig, int begin, int end) {
        return null;
    }
}
