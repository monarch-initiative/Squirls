package org.monarchinitiative.squirls.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.Strand;
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
import org.monarchinitiative.squirls.ingest.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.ingest.conservation.BigWigIngestDao;
import org.monarchinitiative.squirls.ingest.kmers.KmerIngestDao;
import org.monarchinitiative.squirls.ingest.pwm.PwmIngestDao;
import org.monarchinitiative.squirls.ingest.reference.GenomeAssemblyDownloader;
import org.monarchinitiative.squirls.ingest.reference.ReferenceDictionaryIngestDao;
import org.monarchinitiative.squirls.ingest.reference.ReferenceSequenceIngestDao;
import org.monarchinitiative.squirls.ingest.transcripts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessorBuilder;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A static class with methods for building the database with SQUIRLS data.
 *
 * @see Main for an example usage
 */
public class SquirlsDataBuilder {

    /**
     * Number of bases upstream and downstream that we store in addition to storing gene's reference FASTA sequence.
     */
    public static final int GENE_SEQUENCE_PADDING = 500;

    public static final String DONOR_NAME = "SPLICE_DONOR_SITE";

    public static final String ACCEPTOR_NAME = "SPLICE_ACCEPTOR_SITE";

    private static final String LOCATIONS = "classpath:db/migration";

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsDataBuilder.class);

    private SquirlsDataBuilder() {
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
     * Insert reference sequence of all genes into the database.
     * <p>
     * Group transcripts by the gene symbol, then for each gene:
     * <ul>
     *     <li>find the interval that encompasses all transcript of the gene</li>
     *     <li>extend the interval with upstream and downstream padding</li>
     *     <li>fetch FASTA sequence for the interval</li>
     *     <li>store in the database</li>
     * </ul>
     */
    static void ingestReferenceData(DataSource dataSource,
                                    GenomeSequenceAccessor accessor,
                                    BigWigAccessor phyloPAccessor,
                                    Collection<TranscriptModel> transcripts) {
        // group transcripts by gene symbol
        final Map<String, List<TranscriptModel>> txByGeneSymbol = transcripts.stream()
                .collect(Collectors.groupingBy(TranscriptModel::getGeneSymbol));

        // process all genes
        int updated = 0;
        final ReferenceSequenceIngestDao referenceSequenceDao = new ReferenceSequenceIngestDao(dataSource);
        final BigWigIngestDao phyloPDao = new BigWigIngestDao(dataSource);
        for (Map.Entry<String, List<TranscriptModel>> entry : txByGeneSymbol.entrySet()) {
            final String symbol = entry.getKey();
            final List<TranscriptModel> txs = entry.getValue();
            if (txs.isEmpty()) {
                // no transcript for the gene
                LOGGER.warn("No tx found for gene `{}`", symbol);
                continue;
            }

            GenomePosition begin = null, end = null;
            for (TranscriptModel tx : txs) {
                // inspect begin
                final GenomePosition currentBegin = tx.getTXRegion().getGenomeBeginPos();
                if (begin == null || currentBegin.isLt(begin)) {
                    begin = currentBegin;
                }
                // inspect end
                final GenomePosition currentEnd = tx.getTXRegion().getGenomeEndPos();
                if (end == null || currentEnd.isGt(end)) {
                    end = currentEnd;
                }
            }

            /*
            we're interested in fetching reference sequence and PhyloP scores for this interval
            */
            final GenomeInterval interval = new GenomeInterval(begin, end.differenceTo(begin)).withMorePadding(GENE_SEQUENCE_PADDING);
            // Sequence
            final Optional<SequenceInterval> opt = accessor.fetchSequence(interval);
            if (opt.isEmpty()) {
                LOGGER.warn("Could not fetch sequence for gene {} at {}", symbol, interval);
                continue;
            }
            final SequenceInterval sequenceInterval = opt.get();
            updated += referenceSequenceDao.insertSequence(symbol, sequenceInterval);

            // PhyloP
            final GenomeInterval ppIv = interval.withStrand(Strand.FWD);
            String contig = ppIv.getRefDict().getContigIDToName().get(ppIv.getChr());
            contig = (contig.startsWith("chr")) ? contig : "chr" + contig;
            final float[] phyloPScores = phyloPAccessor.getScores(contig, ppIv.getBeginPos(), ppIv.getEndPos());
            phyloPDao.insertScores(symbol, interval, phyloPScores);
        }

        LOGGER.info("Updated {} rows in reference sequence table", updated);
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
                                     Path hexamerPath, Path septamerPath, Path phyloPPath,
                                     Map<String, byte[]> classifiers,
                                     String versionedAssembly) throws SquirlsException {

        // 0 - deserialize Jannovar transcript databases
        JannovarDataManager manager = JannovarDataManager.fromDirectory(jannovarDbDir);
        Collection<TranscriptModel> transcripts = manager.getAllTranscriptModels();

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

        // 3e - store reference dictionary, transcripts, and reference sequence for genes
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
            ingestTranscripts(dataSource, rd, accessor, transcripts, calculator);

            try (BigWigAccessor phyloPAccessor = new BigWigAccessor(phyloPPath)) {
                LOGGER.info("Storing reference sequences and PhyloP scores for genes");
                ingestReferenceData(dataSource, accessor, phyloPAccessor, transcripts);
            } catch (Exception e) {
                throw new SquirlsException(e);
            }
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 3f - store classifier
        LOGGER.info("Inserting classifiers");
        for (Map.Entry<String, byte[]> entry : classifiers.entrySet()) {
            processClassifier(dataSource, entry.getKey(), entry.getValue());
        }

        // TODO - remove reference genome FASTA & companion files after build

    }

}
