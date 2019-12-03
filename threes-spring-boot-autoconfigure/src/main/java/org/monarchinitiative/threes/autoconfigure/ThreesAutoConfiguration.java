package org.monarchinitiative.threes.autoconfigure;


import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.threes.core.ThreeSRuntimeException;
import org.monarchinitiative.threes.core.Utils;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.data.DbSplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.ic.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.data.sms.DbSmsDao;
import org.monarchinitiative.threes.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.threes.core.scoring.SimpleVariantSplicingEvaluator;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.monarchinitiative.threes.core.scoring.VariantSplicingEvaluator;
import org.monarchinitiative.threes.core.scoring.dense.DenseSplicingAnnotator;
import org.monarchinitiative.threes.core.scoring.sparse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableConfigurationProperties({ThreesProperties.class})
public class ThreesAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreesAutoConfiguration.class);

    private final ThreesProperties properties;

    public ThreesAutoConfiguration(ThreesProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "threesDataDirectory")
    public Path threesDataDirectory() throws UndefinedThreesResourceException {
        final String dataDir = properties.getDataDirectory();
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
    public String threesGenomeAssembly() throws UndefinedThreesResourceException {
        final String assembly = properties.getGenomeAssembly();
        if (assembly == null) {
            throw new UndefinedThreesResourceException("Genome assembly (`--threes.genome-assembly`) is not specified");
        }
        return assembly;
    }


    @Bean
    @ConditionalOnMissingBean(name = "threesDataVersion")
    public String threesDataVersion() throws UndefinedThreesResourceException {
        final String dataVersion = properties.getDataVersion();
        if (dataVersion == null) {
            throw new UndefinedThreesResourceException("Data version (`--threes.data-version`) is not specified");
        }
        return dataVersion;
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
    public VariantSplicingEvaluator variantSplicingEvaluator(GenomeSequenceAccessor genomeSequenceAccessor,
                                                             SplicingTranscriptSource splicingTranscriptSource,
                                                             SplicingAnnotator splicingAnnotator) {
        return new SimpleVariantSplicingEvaluator(genomeSequenceAccessor, splicingTranscriptSource, splicingAnnotator);
    }

    @Bean
    public SplicingAnnotator splicingAnnotator(SplicingPwmData splicingPwmData, SMSCalculator smsCalculator) {
        final SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        final SplicingTranscriptLocator locator = new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters());

        final String splicingEvaluatorType = properties.getSplicingEvaluatorType();
        switch (splicingEvaluatorType) {
            case "sparse":
                // TODO - simplify
                final int maxDistanceExonUpstream = properties.getMaxDistanceExonUpstream();
                final int maxDistanceExonDownstream = properties.getMaxDistanceExonDownstream();
                LOGGER.info("Analyzing variants up to {} bp upstream and {} bp downstream from exon", maxDistanceExonUpstream, maxDistanceExonDownstream);
                final String scorerFactoryType = properties.getSparse().getScorerFactoryType();
                final RawScorerFactory rawScorerFactory = new RawScorerFactory(calculator, smsCalculator, maxDistanceExonDownstream, maxDistanceExonUpstream);
                final ScorerFactory scorerFactory;
                switch (scorerFactoryType) {
                    case "scaling":
                        LOGGER.info("Using scaling scorer factory");
                        ImmutableMap<ScoringStrategy, UnaryOperator<Double>> scalerMap = ImmutableMap.<ScoringStrategy, UnaryOperator<Double>>builder()
                                .put(ScoringStrategy.CANONICAL_DONOR, Utils.sigmoidScaler(0.29, -1))
                                .put(ScoringStrategy.CRYPTIC_DONOR, Utils.sigmoidScaler(-5.52, -1))
                                .put(ScoringStrategy.CRYPTIC_DONOR_IN_CANONICAL_POSITION, Utils.sigmoidScaler(-4.56, -1))
                                .put(ScoringStrategy.CANONICAL_ACCEPTOR, Utils.sigmoidScaler(-1.50, -1))
                                .put(ScoringStrategy.CRYPTIC_ACCEPTOR, Utils.sigmoidScaler(-8.24, -1))
                                .put(ScoringStrategy.CRYPTIC_ACCEPTOR_IN_CANONICAL_POSITION, Utils.sigmoidScaler(-4.59, -1))
                                .put(ScoringStrategy.SMS, UnaryOperator.identity()) // TODO - decide how to scale the scores
                                .build();
                        scorerFactory = new ScalingScorerFactory(rawScorerFactory, scalerMap);
                        break;
                    case "raw":
                        LOGGER.info("Using raw scorer factory");
                        scorerFactory = rawScorerFactory;
                        break;
                    default:
                        LOGGER.error("Unknown `threes.sparse.scorer-factory-type` value `{}`", scorerFactoryType);
                        throw new ThreeSRuntimeException(String.format("Unknown `threes.sparse.scorer-factory-type` value: `%s`", scorerFactoryType));
                }
                LOGGER.info("Using sparse splicing evaluator");
                return new SparseSplicingAnnotator(scorerFactory, locator);
            case "dense":
                LOGGER.info("Using dense splicing evaluator");
                return new DenseSplicingAnnotator(splicingPwmData);
            default:
                LOGGER.error("Unknown `threes.splicing-evaluator-type` value `{}`", splicingEvaluatorType);
                throw new ThreeSRuntimeException(String.format("Unknown `threes.splicing-evaluator-type` value: `%s`", splicingEvaluatorType));
        }
    }


    @Bean
    public GenomeSequenceAccessor genomeSequenceAccessor(ThreesDataResolver threesDataResolver) throws InvalidFastaFileException {
        final GenomeSequenceAccessorBuilder builder = GenomeSequenceAccessorBuilder.builder()
                .setFastaPath(threesDataResolver.genomeFastaPath())
                .setFastaFaiPath(threesDataResolver.genomeFastaFaiPath())
                .setFastaDictPath(threesDataResolver.genomeFastaDictPath());
        switch (properties.getGenomeSequenceAccessorType()) {
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
    public SplicingPwmData splicingPwmData(DataSource threesDatasource) {
        return new DbSplicingPositionalWeightMatrixParser(threesDatasource).getSplicingPwmData();
    }


    @Bean
    public SMSCalculator smsCalculator(DataSource threesDatasource) {
        final DbSmsDao dbSmsDao = new DbSmsDao(threesDatasource);
        return new SMSCalculator(dbSmsDao.getSeptamerMap());
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
