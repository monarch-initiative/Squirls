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
import org.monarchinitiative.squirls.ingest.dao.KmerIngestDao;
import org.monarchinitiative.squirls.ingest.dao.PwmIngestDao;
import org.monarchinitiative.squirls.ingest.dao.ReferenceDictionaryIngestDao;
import org.monarchinitiative.squirls.ingest.dao.TranscriptIngestDao;
import org.monarchinitiative.squirls.ingest.data.GenomeAssemblyDownloader;
import org.monarchinitiative.squirls.ingest.data.UrlResourceDownloader;
import org.monarchinitiative.squirls.ingest.transcripts.JannovarDataManager;
import org.monarchinitiative.squirls.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.squirls.ingest.transcripts.SplicingCalculatorImpl;
import org.monarchinitiative.squirls.ingest.transcripts.TranscriptsIngestRunner;
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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    static Runnable downloadReferenceGenome(URL genomeUrl, Path buildDir, String versionedAssembly, boolean overwrite) {
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        return new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, overwrite);
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

    private static Map<String, byte[]> readClassifiers(Map<String, String> clfs) throws IOException {
        final Map<String, byte[]> classifiers = new HashMap<>();
        for (Map.Entry<String, String> entry : clfs.entrySet()) {
            LOGGER.info("Reading classifier `{}` from `{}`", entry.getKey(), entry.getValue());
            try (final InputStream is = Files.newInputStream(Paths.get(entry.getValue()))) {
                classifiers.put(entry.getKey(), is.readAllBytes());
            }
        }
        return classifiers;
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
    public static void buildDatabase(Path buildDir, URL genomeUrl, URL phylopUrl, Path jannovarDbDir, Path yamlPath,
                                     Path hexamerPath, Path septamerPath,
                                     Map<String, String> classifiers,
                                     String versionedAssembly) throws SquirlsException {

        // 0 - initiate download of reference genome FASTA file & PhyloP bigwig file
        // this is where the reference genome will be downloaded by the commands below
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        Path genomeFastaFaiPath = buildDir.resolve(String.format("%s.fa.fai", versionedAssembly));
        Path genomeFastaDictPath = buildDir.resolve(String.format("%s.fa.dict", versionedAssembly));
        Path phyloPPath = buildDir.resolve(String.format("%s.phylop.bw", versionedAssembly));

        final ExecutorService es = Executors.newFixedThreadPool(2);
        es.submit(downloadReferenceGenome(genomeUrl, buildDir, versionedAssembly, false));
        es.submit(new UrlResourceDownloader(phylopUrl, phyloPPath, false));

        // 1 - deserialize Jannovar transcript databases
        JannovarDataManager manager = JannovarDataManager.fromDirectory(jannovarDbDir);

        // 2a - parse YAML with splicing matrices
        SplicingPwmData splicingPwmData;
        try (InputStream is = Files.newInputStream(yamlPath)) {
            SplicingPositionalWeightMatrixParser parser = new InputStreamBasedPositionalWeightMatrixParser(is);
            splicingPwmData = parser.getSplicingPwmData();
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 2b - parse k-mer maps
        final Map<String, Double> hexamerMap;
        final Map<String, Double> septamerMap;
        try {
            hexamerMap = new FileKMerParser(hexamerPath).getKmerMap();
            septamerMap = new FileKMerParser(septamerPath).getKmerMap();
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 3 - create and fill the database
        // 3a - initialize database
        Path databasePath = buildDir.resolve(String.format("%s.splicing", versionedAssembly));
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

        // 3e - store classifier
        try {
            LOGGER.info("Inserting classifiers");
            final Map<String, byte[]> clfData = readClassifiers(classifiers);
            for (Map.Entry<String, byte[]> entry : clfData.entrySet()) {
                processClassifier(dataSource, entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // now wait until the downloads are finished
        try {
            es.shutdown();
            System.out.print("Waiting for the downloads to finish ");
            while (!es.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.print('.');
            }
            System.out.print('\n');
        } catch (InterruptedException e) {
            LOGGER.info("Interrupting the download");
            es.shutdownNow();
        }

        // 3f - store reference dictionary and transcripts
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


    }

}
