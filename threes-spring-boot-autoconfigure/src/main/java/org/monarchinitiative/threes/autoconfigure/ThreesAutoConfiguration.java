package org.monarchinitiative.threes.autoconfigure;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.threes.core.VariantSplicingEvaluator;
import org.monarchinitiative.threes.core.VariantSplicingEvaluatorImpl;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.io.Deserializer;
import org.monarchinitiative.threes.core.data.ClassifierDao;
import org.monarchinitiative.threes.core.data.DbSplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.data.ic.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.data.kmer.DbKMerDao;
import org.monarchinitiative.threes.core.scoring.DenseSplicingAnnotator;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.monarchinitiative.threes.core.scoring.conservation.BigWigAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessorBuilder;
import xyz.ielis.hyperutil.reference.fasta.InvalidFastaFileException;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;


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
    @ConditionalOnMissingBean(name = "phylopBigwigPath")
    public Path phylopBigwigPath() throws UndefinedThreesResourceException {
        if (properties.getPhylopBigwigPath() == null) {
            throw new UndefinedThreesResourceException("Path to PhyloP bigwig file is not specified");
        }
        return Paths.get(properties.getPhylopBigwigPath());
    }


    @Bean
    public BigWigAccessor phylopBigwigAccessor(Path phylopBigwigPath) throws IOException {
        LOGGER.info("Using phyloP bigwig file at `{}`", phylopBigwigPath);
        return new BigWigAccessor(phylopBigwigPath);
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
                                                             SplicingAnnotator splicingAnnotator,
                                                             OverlordClassifier classifier) {
        return new VariantSplicingEvaluatorImpl(genomeSequenceAccessor, splicingTranscriptSource, splicingAnnotator, classifier);

    }

    @Bean
    public SplicingAnnotator splicingAnnotator(SplicingPwmData splicingPwmData,
                                               DbKMerDao dbKMerDao,
                                               BigWigAccessor phylopBigwigAccessor) {
        LOGGER.info("Using dense splicing annotator");
        return new DenseSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap(), phylopBigwigAccessor);
    }

    @Bean
    public DbKMerDao dbKMerDao(@Qualifier("threesDatasource") DataSource threesDatasource) {
        return new DbKMerDao(threesDatasource);
    }

    @Bean
    public OverlordClassifier classifier(@Qualifier("threesDatasource") DataSource threesDatasource) throws CorruptedThreesResourceException {
        final ClassifierDao clfDao = new ClassifierDao(threesDatasource);
        final String clfVersion = properties.getClassifierVersion();
        LOGGER.info("Reading classifier `{}` from database", clfVersion);
        final byte[] bytes = clfDao.readClassifier(clfVersion);
        if (bytes == null) {
            final Collection<String> avail = clfDao.getAvailableClassifiers();
            throw new CorruptedThreesResourceException(String.format("Classifier `%s` is not available. Available: %s",
                    clfVersion, avail.stream().sorted().collect(Collectors.joining(", ", "[ ", " ]"))));
        }

        try {
            return Deserializer.deserialize(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new CorruptedThreesResourceException(e.getMessage());
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
        return builder.build();
    }

    @Bean
    public SplicingPwmData splicingPwmData(DataSource threesDatasource) {
        return new DbSplicingPositionalWeightMatrixParser(threesDatasource).getSplicingPwmData();
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
