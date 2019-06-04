package org.monarchinitiative.sss.ingest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.sss.core.pwm.PositionalWeightMatrixParser;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.GenomeSequenceAccessor;
import org.monarchinitiative.sss.core.reference.SimpleGenomeSequenceAccessor;
import org.monarchinitiative.sss.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.sss.ingest.transcripts.SplicingCalculatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 *
 */
@Configuration
public class IngestConfiguration {

    private final IngestProperties ingestProperties;

    public IngestConfiguration(IngestProperties ingestProperties) {
        this.ingestProperties = ingestProperties;
    }

    @Bean
    public DataSource dataSource() {
        DbConfig dbConfig = ingestProperties.getDbConfig();
        if (dbConfig.getPath() == null || dbConfig.getPath().isEmpty()) {
            throw new IllegalArgumentException("db-config.path has not been specified");
        }

        String jdbcUrl = String.format("jdbc:h2:file:%s;%s", dbConfig.getPath(), dbConfig.getStartupArgs());
        final HikariConfig config = new HikariConfig();
        config.setUsername(dbConfig.getUser());
        config.setPassword(dbConfig.getPassword());
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }


    @Bean
    public JannovarData jannovarData() throws SerializationException {
        return new JannovarDataSerializer(ingestProperties.getJannovarCachePath().toString()).load();
    }

    @Bean
    public GenomeSequenceAccessor genomeSequenceAccessor() {
        return new SimpleGenomeSequenceAccessor(ingestProperties.getFastaPath().toFile(), ingestProperties.getFastaIndexPath().toFile());
    }

    @Bean
    public SplicingCalculator splicingCalculator(GenomeSequenceAccessor genomeSequenceAccessor,
                                                 SplicingInformationContentAnnotator splicingInformationContentAnnotator) {
        return new SplicingCalculatorImpl(genomeSequenceAccessor, splicingInformationContentAnnotator);
    }

    @Bean
    public SplicingInformationContentAnnotator splicingInformationContentAnnotator(PositionalWeightMatrixParser positionalWeightMatrixParser) {
        return new SplicingInformationContentAnnotator(positionalWeightMatrixParser.getDonorMatrix(),
                positionalWeightMatrixParser.getAcceptorMatrix(),
                positionalWeightMatrixParser.getSplicingParameters());
    }

    @Bean
    public PositionalWeightMatrixParser positionalWeightMatrixParser() throws IOException {
        try (InputStream is = Files.newInputStream(ingestProperties.getSplicingInformationContentMatrixPath())) {
            return new PositionalWeightMatrixParser(is);
        }
    }
}
