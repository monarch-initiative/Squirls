package org.monarchinitiative.threes.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.threes.core.ThreeSException;
import org.monarchinitiative.threes.core.data.ClassifierDao;
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.data.kmer.FileKMerParser;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.ingest.kmers.KmerIngestDao;
import org.monarchinitiative.threes.ingest.pwm.PwmIngestDao;
import org.monarchinitiative.threes.ingest.reference.GenomeAssemblyDownloader;
import org.monarchinitiative.threes.ingest.reference.ReferenceDictionaryIngestDao;
import org.monarchinitiative.threes.ingest.transcripts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessorBuilder;

import javax.sql.DataSource;
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
public class ThreesDataBuilder {

    public static final String DONOR_NAME = "SPLICE_DONOR_SITE";

    public static final String ACCEPTOR_NAME = "SPLICE_ACCEPTOR_SITE";

    private static final String LOCATIONS = "classpath:db/migration";

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreesDataBuilder.class);

    private ThreesDataBuilder() {
        // private no-op
    }

    private static int applyMigrations(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(LOCATIONS)
                .load();
        return flyway.migrate();
    }

    private static DataSource makeDataSource(Path databasePath) {
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
     * Store classifier data
     *
     * @param dataSource data source for a database
     * @param clfVersion classifier version
     * @param clfBytes   classifier data
     */
    private static void processClassifier(DataSource dataSource, String clfVersion, byte[] clfBytes) {
        LOGGER.info("Inserting classifier `{}`", clfVersion);
        final ClassifierDao clfDao = new ClassifierDao(dataSource);
        final int updated = clfDao.storeClassifier(clfVersion, clfBytes);
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
     * @throws ThreeSException if anything goes wrong
     */
    public static void buildDatabase(Path buildDir, URL genomeUrl, Path jannovarDbDir, Path yamlPath,
                                     Path hexamerPath, Path septamerPath,
                                     Map<String, byte[]> classifiers,
                                     String versionedAssembly) throws ThreeSException {

        // 0 - deserialize Jannovar transcript databases
        JannovarDataManager manager = JannovarDataManager.fromDirectory(jannovarDbDir);

        // 1a - parse YAML with splicing matrices
        SplicingPwmData splicingPwmData;
        try (InputStream is = Files.newInputStream(yamlPath)) {
            SplicingPositionalWeightMatrixParser parser = new InputStreamBasedPositionalWeightMatrixParser(is);
            splicingPwmData = parser.getSplicingPwmData();
        } catch (IOException e) {
            throw new ThreeSException(e);
        }

        // 1b - parse k-mer maps
        final Map<String, Double> hexamerMap;
        final Map<String, Double> septamerMap;
        try {
            hexamerMap = new FileKMerParser(hexamerPath).getKmerMap();
            septamerMap = new FileKMerParser(septamerPath).getKmerMap();
        } catch (IOException e) {
            throw new ThreeSException(e);
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
            throw new ThreeSException(e);
        }

        // 3f - store classifier
        for (Map.Entry<String, byte[]> entry : classifiers.entrySet()) {
            processClassifier(dataSource, entry.getKey(), entry.getValue());
        }

    }

}
