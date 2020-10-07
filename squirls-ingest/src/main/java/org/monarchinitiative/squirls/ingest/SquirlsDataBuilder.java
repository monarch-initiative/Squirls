package org.monarchinitiative.squirls.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.squirls.core.SquirlsException;
import org.monarchinitiative.squirls.core.classifier.io.Deserializer;
import org.monarchinitiative.squirls.core.classifier.io.OverallModelData;
import org.monarchinitiative.squirls.core.classifier.io.PredictionTransformationParameters;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.PredictionTransformer;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.RegularLogisticRegression;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.SimpleLogisticRegression;
import org.monarchinitiative.squirls.core.data.DbClassifierDataManager;
import org.monarchinitiative.squirls.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.data.kmer.FileKMerParser;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.squirls.ingest.kmers.KmerIngestDao;
import org.monarchinitiative.squirls.ingest.pwm.PwmIngestDao;
import org.monarchinitiative.squirls.ingest.reference.GenomeAssemblyDownloader;
import org.monarchinitiative.squirls.ingest.reference.ReferenceDictionaryIngestDao;
import org.monarchinitiative.squirls.ingest.transcripts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessorBuilder;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * A static class with methods for building splicing database.
 *
 * @see Main for an example usage
 */
public class SquirlsDataBuilder {

    public static final String DONOR_NAME = "SPLICE_DONOR_SITE";

    public static final String ACCEPTOR_NAME = "SPLICE_ACCEPTOR_SITE";

    private static final String LOCATIONS = "classpath:db/migration";

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsDataBuilder.class);

    private SquirlsDataBuilder() {
        // private no-op
    }

    static int applyMigrations(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(LOCATIONS)
                .load();
        return flyway.migrate();
    }

    static DataSource makeDataSource(Path databasePath) {
        // TODO(optional) - add JDBC parameters?
        String jdbcUrl = String.format("jdbc:h2:file:%s", databasePath.toString());
        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    /**
     * Store given <code>donor</code>, <code>acceptor</code>, and <code>parameters</code> into <code>dataSource</code>>.
     *
     * @param dataSource      {@link DataSource} where to matrices will be stored
     * @param splicingPwmData {@link SplicingPwmData} with data representing splice sites
     */
    private static void processPwms(DataSource dataSource, SplicingPwmData splicingPwmData) {
        PwmIngestDao pwmIngestDao = new PwmIngestDao(dataSource);
        SplicingParameters parameters = splicingPwmData.getParameters();
        pwmIngestDao.insertDoubleMatrix(splicingPwmData.getDonor(), DONOR_NAME, parameters.getDonorExonic(), parameters.getDonorIntronic());
        pwmIngestDao.insertDoubleMatrix(splicingPwmData.getAcceptor(), ACCEPTOR_NAME, parameters.getAcceptorExonic(), parameters.getAcceptorIntronic());
    }

    /**
     * Download, uncompress, and concatenate contigs into a single FASTA file. Then, index the FASTA file.
     *
     * @param genomeUrl         url pointing to reference genome FASTA file to be downloaded
     * @param buildDir          path to directory where 3S data files will be created
     * @param versionedAssembly a string like `1710_hg19`, etc.
     * @param overwrite         overwrite existing FASTA file if true
     */
    static void downloadReferenceGenome(URL genomeUrl, Path buildDir, String versionedAssembly, boolean overwrite) {
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        GenomeAssemblyDownloader downloader = new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, overwrite);
        downloader.run(); // run on the same thread
    }

    /**
     * Process given <code>transcripts</code>.
     */
    static void ingestTranscripts(DataSource dataSource,
                                  ReferenceDictionary referenceDictionary,
                                  GenomeSequenceAccessor accessor,
                                  Collection<TranscriptModel> transcripts,
                                  SplicingInformationContentCalculator calculator) {
        TranscriptIngestDao transcriptIngestDao = new TranscriptIngestDao(dataSource, referenceDictionary);
        SplicingCalculator splicingCalculator = new SplicingCalculatorImpl(accessor, calculator);
        TranscriptsIngestRunner transcriptsIngestRunner = new TranscriptsIngestRunner(splicingCalculator, transcriptIngestDao, transcripts);
        transcriptsIngestRunner.run();
    }

    /**
     * Store data for septamers and hexamer methods.
     *
     * @param dataSource  data source for a database
     * @param hexamerMap  map with hexamer scores
     * @param septamerMap map with septamer scores
     */
    private static void processKmers(DataSource dataSource, Map<String, Double> hexamerMap, Map<String, Double> septamerMap) {
        KmerIngestDao dao = new KmerIngestDao(dataSource);
        int updated = dao.insertHexamers(hexamerMap);
        LOGGER.info("Updated {} rows in hexamer table", updated);
        updated = dao.insertSeptamers(septamerMap);
        LOGGER.info("Updated {} rows in septamer table", updated);
    }

    /**
     * Serialize data required to construct {@link org.monarchinitiative.squirls.core.VariantSplicingEvaluator}
     *
     * @param dataSource data source for a database
     * @param clfVersion classifier version
     * @param clfBytes   all data required to construct {@link org.monarchinitiative.squirls.core.VariantSplicingEvaluator}
     */
    private static void processClassifier(DataSource dataSource, String clfVersion, byte[] clfBytes) {
        final DbClassifierDataManager manager = new DbClassifierDataManager(dataSource);
        final OverallModelData data = Deserializer.deserializeOverallModelData(new ByteArrayInputStream(clfBytes));

        // squirls classifier
        LOGGER.info("Inserting classifier `{}`", clfVersion);
        int updated = manager.storeClassifier(clfVersion, clfBytes);

        // prediction transformer
        final PredictionTransformationParameters params = data.getLogisticRegressionParameters();
        final PredictionTransformer transformer;
        if (params.getSlope().get(0).size() < 2) {
            // this must be simple logistic regression
            double slope = params.getSlope().get(0).get(0);
            double intercept = params.getInterceptScalar();
            transformer = SimpleLogisticRegression.getInstance(slope, intercept);
        } else {
            // this is regular logistic regression
            transformer = RegularLogisticRegression.getInstance(params.getDonorSlope(), params.getAcceptorSlope(), params.getInterceptScalar());
        }

        updated += manager.storeTransformer(clfVersion, transformer);

        LOGGER.info("Updated {} rows", updated);
    }

    /**
     * Build the database given inputs.
     *
     * @param buildDir          path to directory where the database file should be stored
     * @param genomeUrl         url pointing to `tar.gz` file with reference genome
     * @param jannovarDbDir     path to directory with Jannovar serialized files
     * @param yamlPath          path to file with splice site definitions
     * @param versionedAssembly a string like `1710_hg19`, etc.
     * @throws SquirlsException if anything goes wrong
     */
    public static void buildDatabase(Path buildDir, URL genomeUrl, Path jannovarDbDir, Path yamlPath,
                                     Path hexamerPath, Path septamerPath,
                                     Map<String, byte[]> classifiers,
                                     String versionedAssembly) throws SquirlsException {

        // 0 - deserialize Jannovar transcript databases
        JannovarDataManager manager = JannovarDataManager.fromDirectory(jannovarDbDir);

        // 1a - parse YAML with splicing matrices
        SplicingPwmData splicingPwmData;
        try (InputStream is = Files.newInputStream(yamlPath)) {
            SplicingPositionalWeightMatrixParser parser = new InputStreamBasedPositionalWeightMatrixParser(is);
            splicingPwmData = parser.getSplicingPwmData();
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 1b - parse k-mer maps
        final Map<String, Double> hexamerMap;
        final Map<String, Double> septamerMap;
        try {
            hexamerMap = new FileKMerParser(hexamerPath).getKmerMap();
            septamerMap = new FileKMerParser(septamerPath).getKmerMap();
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 2 - download reference genome FASTA file
        downloadReferenceGenome(genomeUrl, buildDir, versionedAssembly, false);

        // this is where the reference genome will be downloaded by the command above
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        Path genomeFastaFaiPath = buildDir.resolve(String.format("%s.fa.fai", versionedAssembly));
        Path genomeFastaDictPath = buildDir.resolve(String.format("%s.fa.dict", versionedAssembly));


        // 3 - create and fill the database
        // 3a - initialize database
        Path databasePath = buildDir.resolve(String.format("%s_splicing", versionedAssembly));
        LOGGER.info("Creating database at `{}`", databasePath);
        DataSource dataSource = makeDataSource(databasePath);

        // 3b - apply migrations
        final int i = applyMigrations(dataSource);
        LOGGER.info("Applied {} migrations", i);

        // 3c - store PWM data
        LOGGER.info("Inserting PWMs");
        processPwms(dataSource, splicingPwmData);

        // 3d - store k-mer maps
        LOGGER.info("Inserting k-mer maps");
        processKmers(dataSource, hexamerMap, septamerMap);

        // 3e - store reference dictionary and transcripts
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        try (GenomeSequenceAccessor accessor = GenomeSequenceAccessorBuilder.builder()
                .setFastaPath(genomeFastaPath)
                .setFastaFaiPath(genomeFastaFaiPath)
                .setFastaDictPath(genomeFastaDictPath)
                .build()) {
            final ReferenceDictionary rd = accessor.getReferenceDictionary();
            LOGGER.info("Inserting reference dictionary");
            ReferenceDictionaryIngestDao referenceDictionaryIngestDao = new ReferenceDictionaryIngestDao(dataSource);
            referenceDictionaryIngestDao.saveReferenceDictionary(rd);

            LOGGER.info("Inserting transcripts");
            ingestTranscripts(dataSource, rd, accessor, manager.getAllTranscriptModels(), calculator);
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 3f - store classifier
        LOGGER.info("Inserting classifiers");
        for (Map.Entry<String, byte[]> entry : classifiers.entrySet()) {
            processClassifier(dataSource, entry.getKey(), entry.getValue());
        }

    }

}
