package org.monarchinitiative.threes.autoconfigure;


import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.threes.core.Utils;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.data.DbSplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.ic.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.sms.DbSmsDao;
import org.monarchinitiative.threes.core.data.sms.SMSParser;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessorBuilder;
import xyz.ielis.hyperutil.reference.fasta.InvalidFastaFileException;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;


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
    @ConditionalOnMissingBean(name = "threesGenomeAssembly")
    public String threesGenomeAssembly(Environment environment) throws UndefinedThreesResourceException {
        String assembly = environment.getProperty("threes.genome-assembly");
        if (assembly == null) {
            throw new UndefinedThreesResourceException("Genome assembly (`--threes.genome-assembly`) is not specified");
        }
        return assembly;
    }


    @Bean
    @ConditionalOnMissingBean(name = "threesDataVersion")
    public String threesDataVersion(Environment environment) throws UndefinedThreesResourceException {
        String dataVersion = environment.getProperty("threes.data-version");
        if (dataVersion == null) {
            throw new UndefinedThreesResourceException("Data version (`--threes.data-version`) is not specified");
        }
        return dataVersion;
    }

    @Bean
    @ConditionalOnMissingBean(name = "scorerFactoryType")
    public String scorerFactoryType(Environment environment) {
        return environment.getProperty("threes.scorer-factory-type", "scaling");
    }

    @Bean
    @ConditionalOnMissingBean(name = "genomeSequenceAccessorType")
    public String genomeSequenceAccessorType(Environment environment) {
        return environment.getProperty("threes.genome-sequence-accessor-type", "simple");
    }


    @Bean(name = "maxDistanceExonUpstream")
    public int maxDistanceExonUpstream(Environment environment) {
        String distance = environment.getProperty("threes.max-distance-exon-upstream", "50");
        LOGGER.info("Analyzing variants up to {}bp upstream from exon", distance);
        return Integer.parseInt(distance);
    }


    @Bean(name = "maxDistanceExonDownstream")
    public int maxDistanceExonDownstream(Environment environment) {
        String distance = environment.getProperty("threes.max-distance-exon-downstream", "50");
        LOGGER.info("Analyzing variants up to {}bp downstream from exon", distance);
        return Integer.parseInt(distance);
    }


    @Bean
    public ThreesDataResolver threesDataResolver(Path threesDataDirectory, String threesGenomeAssembly, String threesDataVersion) {
        return new ThreesDataResolver(threesDataDirectory, threesDataVersion, threesGenomeAssembly);
    }


    @Bean
    public SplicingTranscriptSource splicingTranscriptSource(DataSource threesDatasource) {
        return new DbSplicingTranscriptSource(threesDatasource);
    }


    @Bean
    public SplicingEvaluator splicingEvaluator(SplicingTranscriptLocator splicingTranscriptLocator, ScorerFactory scorerFactory) {
        return new SimpleSplicingEvaluator(scorerFactory, splicingTranscriptLocator);
    }


    @Bean
    public ScorerFactory scorerFactory(SplicingInformationContentCalculator splicingInformationContentAnnotator,
                                       SMSCalculator smsCalculator,
                                       String scorerFactoryType,
                                       int maxDistanceExonDownstream, int maxDistanceExonUpstream) {
        RawScorerFactory rawScorerFactory = new RawScorerFactory(splicingInformationContentAnnotator, smsCalculator, maxDistanceExonDownstream, maxDistanceExonUpstream);

        switch (scorerFactoryType) {
            case "raw":
                LOGGER.info("Using raw scorer factory");
                return rawScorerFactory;
            default:
                LOGGER.warn("Unknown scorer factory type '{}', using scaling scorer factory", scorerFactoryType);
            case "scaling":
                LOGGER.info("Using scaling scorer factory");
                // TODO - MANY HARDCODED VALUES ARE PRESENT HERE
                ImmutableMap<ScoringStrategy, UnaryOperator<Double>> scalerMap = ImmutableMap.<ScoringStrategy, UnaryOperator<Double>>builder()
                        .put(ScoringStrategy.CANONICAL_DONOR, Utils.sigmoidScaler(0.29, -1))
                        .put(ScoringStrategy.CRYPTIC_DONOR, Utils.sigmoidScaler(-5.52, -1))
                        .put(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, Utils.sigmoidScaler(-4.56, -1))
                        .put(ScoringStrategy.CANONICAL_ACCEPTOR, Utils.sigmoidScaler(-1.50, -1))
                        .put(ScoringStrategy.CRYPTIC_ACCEPTOR, Utils.sigmoidScaler(-8.24, -1))
                        .put(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, Utils.sigmoidScaler(-4.59, -1))
                        .put(ScoringStrategy.SMS, UnaryOperator.identity()) // TODO - decide how to scale the scores
                        .build();
                return new ScalingScorerFactory(rawScorerFactory, scalerMap);
        }

    }

    @Bean
    public GenomeSequenceAccessor genomeSequenceAccessor(ThreesDataResolver threesDataResolver, String genomeSequenceAccessorType) throws InvalidFastaFileException {
        final GenomeSequenceAccessorBuilder builder = GenomeSequenceAccessorBuilder.builder()
                .setFastaPath(threesDataResolver.genomeFastaPath())
                .setFastaFaiPath(threesDataResolver.genomeFastaFaiPath())
                .setFastaDictPath(threesDataResolver.genomeFastaDictPath());
        switch (genomeSequenceAccessorType) {
            case "chromosome":
                LOGGER.info("Using single chromosome genome sequence accessor");
                builder.setType(GenomeSequenceAccessor.Type.SINGLE_CHROMOSOME);
                break;
            case "simple":
            default:
                LOGGER.info("Using simple genome sequence accessor");
                break;
        }
        return builder
                .build();
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
    public SplicingParameters splicingParameters(SplicingInformationContentCalculator splicingInformationContentAnnotator) {
        return splicingInformationContentAnnotator.getSplicingParameters();
    }


    @Bean
    public SplicingInformationContentCalculator splicingInformationContentAnnotator(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return new SplicingInformationContentCalculator(splicingPositionalWeightMatrixParser.getSplicingPwmData());
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

        String jdbcUrl = String.format("jdbc:h2:file:%s;ACCESS_MODE_DATA=r", datasourcePath);
        final HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setPoolName("threes-pool");

        return new HikariDataSource(config);
    }
}
