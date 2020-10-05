package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DbSplicingAnnotationDataSource implements SplicingAnnotationDataSource {

    private final DataSource dataSource;

    private final ReferenceDictionary rd;

    public DbSplicingAnnotationDataSource(DataSource dataSource, ReferenceDictionary rd) {
        this.dataSource = dataSource;
        this.rd = rd;
    }


    @Override
    public Collection<String> getTranscriptAccessionIds() {
        // TODO: 10/5/20 implement
        return List.of();
    }

    @Override
    public <T extends SplicingPredictionData> Collection<T> getAnnotations(GenomeVariant variant) {
        // TODO: 10/5/20 implement
        return List.of();
    }

    @Override
    public <T extends SplicingPredictionData> Collection<T> getAnnotations(GenomeVariant variant, Set<String> transcripts) {
        // TODO: 10/5/20 implement
        return List.of();
    }
}
