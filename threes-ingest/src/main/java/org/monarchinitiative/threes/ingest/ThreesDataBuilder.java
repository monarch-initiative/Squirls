package org.monarchinitiative.threes.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.threes.core.ThreeSException;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
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
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static void processContigs(DataSource dataSource, Map<String, Integer> contigLengths) {
        ContigIngestDao contigIngestDao = new ContigIngestDao(dataSource);
        ContigIngestRunner contigIngestRunner = new ContigIngestRunner(contigIngestDao, contigLengths);
        contigIngestRunner.run();
    }

    /**
     * Process given <code>transcripts</code>.
     */
    public static void processTranscripts(DataSource dataSource,
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
     * @param dataSource      {@link DataSource} where to matrices will be stored
     * @param splicingPwmData {@link SplicingPwmData} with data representing splice sites
     */
    public static void processPwms(DataSource dataSource, SplicingPwmData splicingPwmData) {
        final PwmIngestDao pwmIngestDao = new PwmIngestDao(dataSource);
        SplicingParameters parameters = splicingPwmData.getParameters();
        pwmIngestDao.insertDoubleMatrix(splicingPwmData.getDonor(), DONOR_NAME, parameters.getDonorExonic(), parameters.getDonorIntronic());
        pwmIngestDao.insertDoubleMatrix(splicingPwmData.getAcceptor(), ACCEPTOR_NAME, parameters.getAcceptorExonic(), parameters.getAcceptorIntronic());
    }

    private static String getVersionedAssembly(String assembly, String version) throws ThreeSException {
        assembly = normalizeAssemblyString(assembly);
        // a string like `1902_hg19`
        return version + "_" + assembly;
    }

    /**
     * Download, uncompress, and concatenate contigs into a single FASTA file. Then, index the FASTA file.
     *
     * @param genomeUrl url pointing to reference genome FASTA file to be downloaded
     * @param buildDir  path to directory where 3S data files will be created
     * @param assembly  a string like `hg19`, `hg38`, `GRCh37`, `GRCh38`, `grch37`, etc.
     * @param version   version of the database, e.g. `1902`
     * @param overwrite overwrite existing FASTA file if true
     * @throws ThreeSException if something goes wrong
     */
    public static void downloadReferenceGenome(URL genomeUrl, Path buildDir, String assembly, String version, boolean overwrite) throws ThreeSException {
        String versionedAssembly = getVersionedAssembly(assembly, version);
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        GenomeAssemblyDownloader downloader = new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, overwrite);
        downloader.run(); // run on the same thread
    }

    /**
     * Build splicing database using provided data.
     *
     * @param jannovarData     {@link JannovarData} with transcripts to use for database
     * @param transcriptSource string telling which transcript source is being used within <code>jannovarData</code>
     * @param accessor         {@link GenomeSequenceAccessor} for fetching nucleotide sequences from reference genome
     * @param splicingPwmData  {@link SplicingPwmData} with representations of splice sites
     * @param buildDir         path to directory where the final database will be stored
     * @param assembly         a string like `hg19`, `hg38`, `GRCh37`, `GRCh38`, `grch37`, etc.
     * @param version          version of the database, e.g. `1902`
     * @throws ThreeSException when input sanity checks fail
     */
    public static void buildThreesDatabase(JannovarData jannovarData,
                                           String transcriptSource,
                                           GenomeSequenceAccessor accessor,
                                           SplicingPwmData splicingPwmData,
                                           Path buildDir,
                                           String assembly,
                                           String version) throws ThreeSException {
        String versionedAssembly = getVersionedAssembly(assembly, version);

        // 2 - create database
        // 2a - initialize database
        Path databasePath = buildDir.resolve(String.format("%s_splicing_%s", versionedAssembly, transcriptSource));
        final DataSource dataSource = makeDataSource(databasePath);

        // 2b - apply migrations
        int migrations = applyMigrations(dataSource);
        LOGGER.info("Applied {} migrations", migrations);

        Map<String, Integer> contigLengths = jannovarData.getRefDict().getContigNameToID().keySet().stream()
                .collect(Collectors.toMap(Function.identity(),
                        idx -> jannovarData.getRefDict().getContigIDToLength().get(jannovarData.getRefDict().getContigNameToID().get(idx))));

        // 3 - store info regarding chromosomes into the database
        processContigs(dataSource, contigLengths);

        // 4 - parse splicing PWM data
        processPwms(dataSource, splicingPwmData);

        // 5 - process transcripts
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        processTranscripts(dataSource, accessor, contigLengths, jannovarData.getTmByAccession().values(), calculator);
    }
}
