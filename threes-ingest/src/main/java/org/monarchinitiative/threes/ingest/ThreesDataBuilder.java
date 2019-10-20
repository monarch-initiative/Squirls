package org.monarchinitiative.threes.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.flywaydb.core.Flyway;
import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.ThreeSException;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.threes.ingest.pwm.PwmIngestDao;
import org.monarchinitiative.threes.ingest.reference.ContigIngestDao;
import org.monarchinitiative.threes.ingest.reference.ContigIngestRunner;
import org.monarchinitiative.threes.ingest.reference.GenomeAssemblyDownloader;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculatorImpl;
import org.monarchinitiative.threes.ingest.transcripts.TranscriptIngestDao;
import org.monarchinitiative.threes.ingest.transcripts.TranscriptsIngestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class for building splicing database for given <code>transcriptSource</code> and <code>assembly</code>, using
 * provided {@link JannovarData}.
 */
public class ThreesDataBuilder {

    public static final String DONOR_NAME = "SPLICE_DONOR_SITE";

    public static final String ACCEPTOR_NAME = "SPLICE_ACCEPTOR_SITE";

    private static final String LOCATIONS = "classpath:db/migration";

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreesDataBuilder.class);


    private static String normalizeAssemblyString(String assembly) throws ThreeSException {
        switch (assembly.toLowerCase()) {
            case "hg19":
            case "grch37":
                return "hg19";
            case "hg38":
            case "grch38":
                return "hg38";
            default:
                throw new ThreeSException(String.format("Unknown assembly string '%s'", assembly));
        }
    }

    private static DataSource makeDataSource(Path databasePath) {
        // TODO - add JDBC parameters?
        String jdbcUrl = String.format("jdbc:h2:file:%s", databasePath.toString());
        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    private static int applyMigrations(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(ThreesDataBuilder.LOCATIONS)
                .load();
        return flyway.migrate();
    }

    /**
     * Process given <code>transcripts</code>.
     */
    static void processTranscripts(DataSource dataSource,
                                   GenomeSequenceAccessor accessor,
                                   Map<String, Integer> contigLengths,
                                   Collection<TranscriptModel> transcripts,
                                   SplicingInformationContentCalculator calculator) {
        GenomeCoordinatesFlipper genomeCoordinatesFlipper = new GenomeCoordinatesFlipper(contigLengths);
        TranscriptIngestDao transcriptIngestDao = new TranscriptIngestDao(dataSource, genomeCoordinatesFlipper);
        SplicingCalculator splicingCalculator = new SplicingCalculatorImpl(accessor, calculator);
        TranscriptsIngestRunner transcriptsIngestRunner = new TranscriptsIngestRunner(splicingCalculator, transcriptIngestDao, transcripts);
        transcriptsIngestRunner.run();
    }

    /**
     * Store given <code>donor</code>, <code>acceptor</code>, and <code>parameters</code> into <code>dataSource</code>>.
     *
     * @param dataSource {@link DataSource} where to matrices will be stored
     * @param donor      {@link DoubleMatrix} representing splice donor site
     * @param acceptor   {@link DoubleMatrix} representing splice acceptor site
     * @param parameters parameters of splicing sites
     */
    static void processPwms(DataSource dataSource, DoubleMatrix donor, DoubleMatrix acceptor, SplicingParameters parameters) {
        final PwmIngestDao pwmIngestDao = new PwmIngestDao(dataSource);
        pwmIngestDao.insertDoubleMatrix(donor, DONOR_NAME, parameters.getDonorExonic(), parameters.getDonorIntronic());
        pwmIngestDao.insertDoubleMatrix(acceptor, ACCEPTOR_NAME, parameters.getAcceptorExonic(), parameters.getAcceptorIntronic());
    }

    /**
     * @param jannovarData     {@link JannovarData} with transcripts to use for database
     * @param transcriptSource string telling which transcript source is being used within <code>jannovarData</code>
     * @param buildDir         path to directory where 3S data files will be created
     * @param genomeUrl        url pointing to reference genome FASTA file to be downloaded
     * @param assembly         a string like `hg19`, `hg38`, `GRCh37`, `GRCh38`, `grch37`, etc.
     * @param version          version of the database, e.g. `1902`
     * @throws ThreeSException when input sanity checks fail
     */
    public static void build(JannovarData jannovarData,
                             String transcriptSource,
                             Path buildDir,
                             URL genomeUrl,
                             String assembly,
                             String version) throws ThreeSException {
        assembly = normalizeAssemblyString(assembly);
        // a string like `1902_hg19`
        String versionedAssembly = version + "_" + assembly;

        // 1 - download & process FASTA file
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        Path genomeFastaFaiPath = buildDir.resolve(String.format("%s.fa.fai", versionedAssembly));

        GenomeAssemblyDownloader downloader = new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, false);
        downloader.run();

        // 2 - create database
        // 2a - initialize database
        Path databasePath = buildDir.resolve(String.format("%s_splicing_%s", versionedAssembly, transcriptSource));
        final DataSource dataSource = makeDataSource(databasePath);

        // 2b - apply migrations
        int migrations = applyMigrations(dataSource);
        LOGGER.info("Applied {} migrations", migrations);

        // 3 - store info regarding chromosomes into the database
        ContigIngestDao contigIngestDao = new ContigIngestDao(dataSource);
        ContigIngestRunner contigIngestRunner = new ContigIngestRunner(contigIngestDao, jannovarData);
        contigIngestRunner.run();

        // 4 - parse splicing PWMs
        // TODO - externalize?
        Path splicingIcMatrixPath = Paths.get(ThreesDataBuilder.class.getResource("/splicing-information-content-matrix.yaml").getPath());
        final DoubleMatrix donor, acceptor;
        final SplicingParameters parameters;
        try (InputStream pwmIs = Files.newInputStream(splicingIcMatrixPath)) {
            InputStreamBasedPositionalWeightMatrixParser pwmParser = new InputStreamBasedPositionalWeightMatrixParser(pwmIs);
            donor = pwmParser.getDonorMatrix();
            acceptor = pwmParser.getAcceptorMatrix();
            parameters = pwmParser.getSplicingParameters();
            processPwms(dataSource, donor, acceptor, parameters);
        } catch (IOException e) {
            throw new ThreeSException(e);
        }

        // 5 - process transcripts
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(donor, acceptor, parameters);
        try (GenomeSequenceAccessor accessor = new PrefixHandlingGenomeSequenceAccessor(genomeFastaPath, genomeFastaFaiPath)) {
            Map<String, Integer> contigLengths = jannovarData.getRefDict().getContigNameToID().keySet().stream()
                    .collect(Collectors.toMap(Function.identity(),
                            idx -> jannovarData.getRefDict().getContigIDToLength().get(jannovarData.getRefDict().getContigNameToID().get(idx))));
            processTranscripts(dataSource, accessor, contigLengths, jannovarData.getTmByAccession().values(), calculator);
        } catch (Exception e) {
            throw new ThreeSException(e);
        }
    }
}
