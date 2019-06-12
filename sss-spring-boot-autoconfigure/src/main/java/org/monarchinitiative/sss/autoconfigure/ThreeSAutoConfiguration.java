package org.monarchinitiative.sss.autoconfigure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.sss.core.data.ContigLengthDao;
import org.monarchinitiative.sss.core.data.DbSplicingTranscriptSource;
import org.monarchinitiative.sss.core.data.SplicingTranscriptSource;
import org.monarchinitiative.sss.core.pwm.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;
import org.monarchinitiative.sss.core.pwm.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.sss.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.sss.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.sss.core.reference.fasta.InvalidFastaFileException;
import org.monarchinitiative.sss.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.sss.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.sss.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.sss.core.scoring.SimpleSplicingEvaluator;
import org.monarchinitiative.sss.core.scoring.SplicingEvaluator;
import org.monarchinitiative.sss.core.scoring.TranscriptSplicingEvaluator;
import org.monarchinitiative.sss.core.scoring.scorers.ScorerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


/**
 * @author Daniel Danis <daniel.danis@jax.org>
 */
@Configuration
@EnableConfigurationProperties({ThreeSProperties.class})
public class ThreeSAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeSAutoConfiguration.class);

    private final ThreeSProperties threeSProperties;

    public ThreeSAutoConfiguration(ThreeSProperties threeSProperties) {
        this.threeSProperties = threeSProperties;
    }

    @Bean
    public SplicingTranscriptSource splicingTranscriptSource(DataSource sssDataSource, ContigLengthDao contigLengthDao) {
        return new DbSplicingTranscriptSource(sssDataSource, contigLengthDao.getContigLengths());
    }

    @Bean
    @ConditionalOnProperty({"sss.strategy.simple"})
    public SplicingEvaluator simpleSplicingEvaluator(GenomeSequenceAccessor genomeSequenceAccessor,
                                                     SplicingTranscriptLocator splicingTranscriptLocator,
                                                     ScorerFactory scorerFactory) {
        return new SimpleSplicingEvaluator(genomeSequenceAccessor, splicingTranscriptLocator, scorerFactory);
    }


    @Bean
    @ConditionalOnProperty("sss.strategy.transcript")
    public SplicingEvaluator transcriptSplicingEvaluator(GenomeSequenceAccessor genomeSequenceAccessor,
                                                         SplicingTranscriptLocator splicingTranscriptLocator) {
        return new TranscriptSplicingEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public GenomeSequenceAccessor genomeSequenceAccessor(Path genomeFasta, Path genomeFastaFai) throws InvalidFastaFileException {
        return new PrefixHandlingGenomeSequenceAccessor(genomeFasta, genomeFastaFai);
    }

    @Bean
    public Path genomeFasta() {
        return Paths.get(Objects.requireNonNull(threeSProperties.getGenomeFastaPath()));
    }

    @Bean
    public Path genomeFastaFai() {
        return Paths.get(Objects.requireNonNull(threeSProperties.getGenomeFastaFaiPath()));
    }

    @Bean
    @ConditionalOnMissingBean
    public SplicingTranscriptLocator splicingTranscriptLocator(SplicingParameters splicingParameters, GenomeCoordinatesFlipper genomeCoordinatesFlipper) {
        return new NaiveSplicingTranscriptLocator(splicingParameters, genomeCoordinatesFlipper);
    }

    @Bean
    @ConditionalOnMissingBean
    public GenomeCoordinatesFlipper genomeCoordinatesFlipper(ContigLengthDao contigLengthDao) {
        return new GenomeCoordinatesFlipper(contigLengthDao.getContigLengths());
    }

    @Bean
    @ConditionalOnMissingBean
    public ContigLengthDao contigLengthDao(DataSource sssDataSource) {
        return new ContigLengthDao(sssDataSource);
    }


    @Bean
    @ConditionalOnMissingBean
    public SplicingParameters splicingParameters(SplicingInformationContentAnnotator splicingInformationContentAnnotator) {
        return splicingInformationContentAnnotator.getSplicingParameters();
    }


    @Bean
    @ConditionalOnMissingBean
    public SplicingInformationContentAnnotator splicingInformationContentAnnotator(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return new SplicingInformationContentAnnotator(splicingPositionalWeightMatrixParser.getDonorMatrix(), splicingPositionalWeightMatrixParser.getAcceptorMatrix(), splicingPositionalWeightMatrixParser.getSplicingParameters());
    }


    @Bean
    @ConditionalOnMissingBean
    public SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser(DataSource sssDataSource) {
        return new DbSplicingPositionalWeightMatrixParser(sssDataSource);
    }


    @Bean
    @ConditionalOnMissingBean(name = "sssDataSource")
    public DataSource sssDataSource() {
        final DataSourceProperties ds = threeSProperties.getDatasource();
        if (ds.getPath() == null || ds.getPath().isEmpty()) {
            throw new IllegalArgumentException("sss.datasource.path has not been specified");
        }

        String jdbcUrl = String.format("jdbc:h2:file:%s;%s", ds.getPath(), ds.getStartupArgs());
        final HikariConfig config = new HikariConfig();
        config.setUsername(ds.getUsername());
        config.setPassword(ds.getPassword());
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

}
