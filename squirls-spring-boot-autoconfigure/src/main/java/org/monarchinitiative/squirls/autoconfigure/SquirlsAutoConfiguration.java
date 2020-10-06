package org.monarchinitiative.squirls.autoconfigure;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.squirls.core.StandardVariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.IdentityTransformer;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.PredictionTransformer;
import org.monarchinitiative.squirls.core.data.ClassifierDataManager;
import org.monarchinitiative.squirls.core.data.DbClassifierDataManager;
import org.monarchinitiative.squirls.core.data.DbSplicingAnnotationDataSource;
import org.monarchinitiative.squirls.core.data.SplicingAnnotationDataSource;
import org.monarchinitiative.squirls.core.data.ic.CorruptedPwmException;
import org.monarchinitiative.squirls.core.data.ic.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.data.kmer.DbKMerDao;
import org.monarchinitiative.squirls.core.scoring.AGEZSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.DenseSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Auto-configuration of of the Squirls code.
 *
 * @author Daniel Danis <daniel.danis@jax.org>
 */
@Configuration
@EnableConfigurationProperties({
        SquirlsProperties.class,
        ClassifierProperties.class,
        AnnotatorProperties.class})
public class SquirlsAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsAutoConfiguration.class);

    private final SquirlsProperties properties;

    public SquirlsAutoConfiguration(SquirlsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "squirlsDataDirectory")
    public Path squirlsDataDirectory() throws UndefinedSquirlsResourceException {
        final String dataDir = properties.getDataDirectory();
        if (dataDir == null || dataDir.isEmpty()) {
            throw new UndefinedSquirlsResourceException("Path to Squirls data directory (`--squirls.data-directory`) is not specified");
        }
        Path dataDirPath = Paths.get(dataDir);
        if (!Files.isDirectory(dataDirPath)) {
            throw new UndefinedSquirlsResourceException(String.format("Path to Squirls data directory '%s' does not point to real directory", dataDirPath));
        }
        return dataDirPath;
    }


    @Bean
    @ConditionalOnMissingBean(name = "squirlsGenomeAssembly")
    public String squirlsGenomeAssembly() throws UndefinedSquirlsResourceException {
        final String assembly = properties.getGenomeAssembly();
        if (assembly == null) {
            throw new UndefinedSquirlsResourceException("Genome assembly (`--squirls.genome-assembly`) is not specified");
        }
        return assembly;
    }

    @Bean
    @ConditionalOnMissingBean(name = "squirlsDataVersion")
    public String squirlsDataVersion() throws UndefinedSquirlsResourceException {
        final String dataVersion = properties.getDataVersion();
        if (dataVersion == null) {
            throw new UndefinedSquirlsResourceException("Data version (`--squirls.data-version`) is not specified");
        }
        return dataVersion;
    }

    @Bean
    public SquirlsDataResolver squirlsDataResolver(Path squirlsDataDirectory,
                                                   String squirlsGenomeAssembly,
                                                   String squirlsDataVersion) {
        return new SquirlsDataResolver(squirlsDataDirectory, squirlsDataVersion, squirlsGenomeAssembly);
    }

    @Bean
    public SplicingAnnotationDataSource splicingAnnotationDataSource(DataSource squirlsDatasource) {
        return new DbSplicingAnnotationDataSource(squirlsDatasource);
    }

    @Bean
    public VariantSplicingEvaluator variantSplicingEvaluator(SplicingAnnotationDataSource splicingAnnotationDataSource,
                                                             SplicingAnnotator splicingAnnotator,
                                                             ClassifierDataManager classifierDataManager) throws InvalidSquirlsResourceException {
        final ClassifierProperties classifierProperties = properties.getClassifier();

        final String clfVersion = classifierProperties.getVersion();
        final Collection<String> avail = classifierDataManager.getAvailableClassifiers();
        if (!avail.contains(clfVersion)) {
            String msg = String.format("Classifier version `%s` is not available, choose one from `%s`",
                    clfVersion,
                    avail.stream().sorted().collect(Collectors.joining(", ", "[ ", " ]")));
            LOGGER.error(msg);
            throw new InvalidSquirlsResourceException(msg);
        }

        // get classifier
        final Optional<SquirlsClassifier> clfOpt = classifierDataManager.readClassifier(clfVersion);
        final SquirlsClassifier clf;
        if (clfOpt.isPresent()) {
            LOGGER.debug("Using classifier `{}`", clfVersion);
            clf = clfOpt.get();
        } else {
            String msg = String.format("Error when deserializing classifier `%s` from the database", clfVersion);
            throw new InvalidSquirlsResourceException(msg);
        }

        // get transformer
        final Optional<PredictionTransformer> transOpt = classifierDataManager.readTransformer(clfVersion);
        final PredictionTransformer transformer;
        if (transOpt.isPresent()) {
            transformer = transOpt.get();
        } else {
            LOGGER.warn("Transformer for classifier `{}` is not available. Using identity transformer", clfVersion);
            transformer = IdentityTransformer.getInstance();
        }

        // make variant evaluator
        return StandardVariantSplicingEvaluator.builder()
                .annDataSource(splicingAnnotationDataSource)
                .annotator(splicingAnnotator)
                .classifier(clf)
                .transformer(transformer)
                .maxVariantLength(classifierProperties.getMaxVariantLength())
                .build();
    }

    @Bean
    public ClassifierDataManager classifierDataProvider(@Qualifier("squirlsDatasource") DataSource squirlsDatasource) {
        return new DbClassifierDataManager(squirlsDatasource);
    }

    @Bean
    public SplicingAnnotator splicingAnnotator(SplicingPwmData splicingPwmData,
                                               DbKMerDao dbKMerDao) throws UndefinedSquirlsResourceException {
        final AnnotatorProperties annotatorProperties = properties.getAnnotator();
        final String version = annotatorProperties.getVersion();
        LOGGER.debug("Using `{}` splicing annotator", version);
        switch (version) {
            case "dense":
                return new DenseSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap());
            case "agez":
                return new AGEZSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap());
            default:
                throw new UndefinedSquirlsResourceException(String.format("invalid 'squirls.annotator.version' property value: `%s`", version));
        }
    }

    @Bean
    public DbKMerDao dbKMerDao(@Qualifier("squirlsDatasource") DataSource squirlsDatasource) {
        return new DbKMerDao(squirlsDatasource);
    }

    @Bean
    public SplicingPwmData splicingPwmData(DataSource squirlsDatasource) throws InvalidSquirlsResourceException {
        try {
            return new DbSplicingPositionalWeightMatrixParser(squirlsDatasource).getSplicingPwmData();
        } catch (CorruptedPwmException e) {
            throw new InvalidSquirlsResourceException(e);
        }
    }

    @Bean
    public DataSource squirlsDatasource(SquirlsDataResolver squirlsDataResolver) {
        Path datasourcePath = squirlsDataResolver.getDatasourcePath();

        String jdbcUrl = String.format("jdbc:h2:file:%s;ACCESS_MODE_DATA=r", datasourcePath);
        final HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setPoolName("squirls-pool");

        return new HikariDataSource(config);
    }
}
