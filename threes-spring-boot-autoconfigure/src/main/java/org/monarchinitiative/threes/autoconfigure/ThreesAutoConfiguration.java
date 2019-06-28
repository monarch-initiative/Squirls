package org.monarchinitiative.threes.autoconfigure;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.threes.core.calculators.ic.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.calculators.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.calculators.sms.DbSmsDao;
import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.calculators.sms.SMSParser;
import org.monarchinitiative.threes.core.data.ContigLengthDao;
import org.monarchinitiative.threes.core.data.DbSplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.InvalidFastaFileException;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Daniel Danis <daniel.danis@jax.org>
 */
@Configuration
public class ThreesAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreesAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "threesDataDirectory")
    public Path threesDataDirectory(Environment environment) throws UndefinedThreesResourceException {
        String dataDir = environment.getProperty("threes.data-directory");
        if (dataDir == null || dataDir.isEmpty()) {
            throw new UndefinedThreesResourceException("Path to 3S data directory (`--threes.data-directory`) is not specified");
        }
        Path dataDirPath = Paths.get(dataDir);
        if (!Files.isDirectory(dataDirPath)) {
            throw new UndefinedThreesResourceException(String.format("Path to 3S data directory '%s' does not point to real directory", dataDirPath));
        }
        return dataDirPath;
    }


    @Bean
    @ConditionalOnMissingBean(name = "genomeAssembly")
    public String genomeAssembly(Environment environment) throws UndefinedThreesResourceException {
        String assembly = environment.getProperty("threes.genome-assembly");
        if (assembly == null) {
            throw new UndefinedThreesResourceException("Genome assembly (`--threes.genome-assembly`) is not specified");
        }
        return assembly;
    }


    @Bean
    @ConditionalOnMissingBean(name = "dataVersion")
    public String dataVersion(Environment environment) throws UndefinedThreesResourceException {
        String dataVersion = environment.getProperty("threes.data-version");
        if (dataVersion == null) {
            throw new UndefinedThreesResourceException("Data version (`--threes.data-version`) is not specified");
        }
        return dataVersion;
    }


    @Bean
    @ConditionalOnMissingBean(name = "transcriptSource")
    public String transcriptSource(Environment environment) throws UndefinedThreesResourceException {
        String transcriptSource = environment.getProperty("threes.transcript-source");
        if (transcriptSource == null) {
            throw new UndefinedThreesResourceException("Transcript source (`--threes.transcript-source`) is not specified");
        }
        return transcriptSource;
    }

    @Bean
    @ConditionalOnMissingBean(name = "scorerFactoryType")
    public String scorerFactoryType(Environment environment) {
        return environment.getProperty("threes.scorer-factory-type", "scaling");
    }


    @Bean
    public ThreesDataResolver threesDataResolver(Path threesDataDirectory, String genomeAssembly, String dataVersion, String transcriptSource) {
        return new ThreesDataResolver(threesDataDirectory, dataVersion, genomeAssembly, transcriptSource);
    }


    @Bean
    public SplicingTranscriptSource splicingTranscriptSource(DataSource threesDatasource) {
        return new DbSplicingTranscriptSource(threesDatasource);
    }


    @Bean
    public SplicingEvaluator splicingEvaluator(SplicingTranscriptLocator splicingTranscriptLocator, ScorerFactory scorerFactory, GenomeCoordinatesFlipper genomeCoordinatesFlipper) {
        return new SimpleSplicingEvaluator(scorerFactory, splicingTranscriptLocator, genomeCoordinatesFlipper);
    }


    @Bean
    public ScorerFactory scorerFactory(SplicingInformationContentCalculator splicingInformationContentAnnotator,
                                       SMSCalculator smsCalculator,
                                       String scorerFactoryType) {
        switch (scorerFactoryType) {
            case "raw":
                LOGGER.info("Using raw scorer factory");
                return new RawScorerFactory(splicingInformationContentAnnotator, smsCalculator);
            default:
                LOGGER.warn("Unknown scorer factory type '{}', using scaling scorer factory", scorerFactoryType);
            case "scaling":
                LOGGER.info("Using scaling scorer factory");
                return new ScalingScorerFactory(splicingInformationContentAnnotator, smsCalculator);
        }

    }


    @Bean
    public GenomeSequenceAccessor genomeSequenceAccessor(Path genomeFasta, Path genomeFastaFai) throws InvalidFastaFileException {
        return new PrefixHandlingGenomeSequenceAccessor(genomeFasta, genomeFastaFai);
    }


    @Bean
    public Path genomeFasta(ThreesDataResolver threesDataResolver) {
        return threesDataResolver.genomeFastaPath();
    }


    @Bean
    public Path genomeFastaFai(ThreesDataResolver threesDataResolver) {
        return threesDataResolver.genomeFastaFaiPath();
    }


    @Bean
    public SplicingTranscriptLocator splicingTranscriptLocator(SplicingParameters splicingParameters) {
        return new NaiveSplicingTranscriptLocator(splicingParameters);
    }


    @Bean
    public GenomeCoordinatesFlipper genomeCoordinatesFlipper(ContigLengthDao contigLengthDao) {
        return new GenomeCoordinatesFlipper(contigLengthDao.getContigLengths());
    }


    @Bean
    public ContigLengthDao contigLengthDao(DataSource threesDatasource) {
        return new ContigLengthDao(threesDatasource);
    }


    @Bean
    public SplicingParameters splicingParameters(SplicingInformationContentCalculator splicingInformationContentAnnotator) {
        return splicingInformationContentAnnotator.getSplicingParameters();
    }


    @Bean
    public SplicingInformationContentCalculator splicingInformationContentAnnotator(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return new SplicingInformationContentCalculator(splicingPositionalWeightMatrixParser.getDonorMatrix(), splicingPositionalWeightMatrixParser.getAcceptorMatrix(), splicingPositionalWeightMatrixParser.getSplicingParameters());
    }


    @Bean
    public SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser(DataSource threesDatasource) {
        return new DbSplicingPositionalWeightMatrixParser(threesDatasource);
    }


    @Bean
    public SMSCalculator smsCalculator(SMSParser smsParser) {
        return new SMSCalculator(smsParser.getSeptamerMap());
    }


    @Bean
    public SMSParser smsParser(DataSource threesDatasource) {
        return new DbSmsDao(threesDatasource);
    }

    @Bean
    public DataSource threesDatasource(ThreesDataResolver threesDataResolver) {
        Path datasourcePath = threesDataResolver.getDatasourcePath();

        String jdbcUrl = String.format("jdbc:h2:file:%s", datasourcePath);
        final HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }
}
