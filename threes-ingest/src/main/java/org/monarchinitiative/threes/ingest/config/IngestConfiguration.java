package org.monarchinitiative.threes.ingest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.threes.core.pwm.FileBasedSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.threes.core.pwm.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.InvalidFastaFileException;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculatorImpl;
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
    public GenomeSequenceAccessor genomeSequenceAccessor() throws InvalidFastaFileException {
        return new PrefixHandlingGenomeSequenceAccessor(ingestProperties.getFastaPath(), ingestProperties.getFastaIndexPath());
    }

    @Bean
    public SplicingCalculator splicingCalculator(GenomeSequenceAccessor genomeSequenceAccessor,
                                                 SplicingInformationContentAnnotator splicingInformationContentAnnotator) {
        return new SplicingCalculatorImpl(genomeSequenceAccessor, splicingInformationContentAnnotator);
    }

    @Bean
    public SplicingInformationContentAnnotator splicingInformationContentAnnotator(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return new SplicingInformationContentAnnotator(splicingPositionalWeightMatrixParser.getDonorMatrix(),
                splicingPositionalWeightMatrixParser.getAcceptorMatrix(),
                splicingPositionalWeightMatrixParser.getSplicingParameters());
    }

    @Bean
    public SplicingPositionalWeightMatrixParser positionalWeightMatrixParser() throws IOException {
        try (InputStream is = Files.newInputStream(ingestProperties.getSplicingInformationContentMatrixPath())) {
            return new FileBasedSplicingPositionalWeightMatrixParser(is);
        }
    }
}
