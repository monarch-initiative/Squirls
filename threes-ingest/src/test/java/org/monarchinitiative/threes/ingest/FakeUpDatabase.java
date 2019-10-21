package org.monarchinitiative.threes.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.threes.ingest.pwm.PwmIngestDao;
import org.monarchinitiative.threes.ingest.pwm.PwmIngestRunner;
import org.monarchinitiative.threes.ingest.reference.ContigIngestDao;
import org.monarchinitiative.threes.ingest.reference.ContigIngestRunner;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculatorImpl;
import org.monarchinitiative.threes.ingest.transcripts.TranscriptIngestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * These tests are not really meant to be run on other computer than mine. Sorry about that.
 */
@Disabled("This test is run only to generate small database for testing of other programs")
public class FakeUpDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeUpDatabase.class);

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //
    // Databases will be created only if the paths below point to meaningful files
    //

    private static final Path HG19_FASTA_PATH = Paths.get("/home/ielis/dub/genomes/hg19/hg19.fa");

    private static final Path HG19_FASTA_IDX_PATH = Paths.get("/home/ielis/dub/genomes/hg19/hg19.fa.fai");

    private static final Path HG38_FASTA_PATH = Paths.get("/home/ielis/dub/genomes/hg38/hg38.fa");

    private static final Path HG38_FASTA_IDX_PATH = Paths.get("/home/ielis/dub/genomes/hg38/hg38.fa.fai");

    private static final Path HG19_JANNOVAR_DB = Paths.get("/home/ielis/jannovar/v0.28/hg19_ucsc.ser");

    private static final Path HG38_JANNOVAR_DB = Paths.get("/home/ielis/jannovar/v0.28/hg38_ucsc.ser");

    private static final Path SPLICING_IC_MATRIX_PATH = Paths.get("/home/ielis/data/threes/pwm/splicing-information-content-matrix.yaml");

    //
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    /**
     * A database with N selected transcripts will be created
     */
    private static final int N_TRANSCRIPTS = 200;

    private static SplicingInformationContentCalculator splicingInformationContentAnnotator;

    private static DataSource makeDataSource(Path dbPath) {
        String jdbcUrl = String.format("jdbc:h2:file:%s;INIT=CREATE SCHEMA IF NOT EXISTS SPLICING", dbPath);

        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    private static int applyMigrations(DataSource dataSource, String locations) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("SPLICING")
                .locations(locations)
                .load();
        return flyway.migrate();
    }

    @BeforeAll
    static void setUpBefore() throws Exception {
        SplicingPositionalWeightMatrixParser parser;
        try (InputStream is = Files.newInputStream(SPLICING_IC_MATRIX_PATH)) {
            parser = new InputStreamBasedPositionalWeightMatrixParser(is);
        }
        splicingInformationContentAnnotator = new SplicingInformationContentCalculator(parser.getSplicingPwmData());
    }


    @Test
    void makeHg19Database() throws Exception {
        JannovarData jannovarData = new JannovarDataSerializer(HG19_JANNOVAR_DB.toString()).load();
        Path dbPath = Paths.get("/home/ielis/tmp/1902_hg19_splicing_ucsc");
        DataSource dataSource = makeDataSource(dbPath);

        String locations = "classpath:db/migration";
        int migrations = applyMigrations(dataSource, locations);
        LOGGER.info("Applied {} migrations", migrations);

        Map<String, Integer> contigLengths = jannovarData.getRefDict().getContigIDToName().keySet().stream()
                .collect(Collectors.toMap(
                        id -> jannovarData.getRefDict().getContigIDToName().get(id), // key - chromosome number
                        id -> jannovarData.getRefDict().getContigIDToLength().get(id)));// value - chromosome length

        ContigIngestDao dao = new ContigIngestDao(dataSource);
        ContigIngestRunner contigIngestRunner = new ContigIngestRunner(dao, contigLengths);
        contigIngestRunner.run();

        // process PWMs
        PwmIngestDao pwm = new PwmIngestDao(dataSource);
        PwmIngestRunner pwmIngestRunner = new PwmIngestRunner(pwm, SPLICING_IC_MATRIX_PATH);
        pwmIngestRunner.run();

        // process transcripts
        try (GenomeSequenceAccessor genomeSequenceAccessor = new PrefixHandlingGenomeSequenceAccessor(HG19_FASTA_PATH, HG19_FASTA_IDX_PATH)) {
            final GenomeCoordinatesFlipper genomeCoordinatesFlipper = new GenomeCoordinatesFlipper(contigLengths);
            final TranscriptIngestDao transcriptIngestDao = new TranscriptIngestDao(dataSource, genomeCoordinatesFlipper);
            SplicingCalculator splicingCalculator = new SplicingCalculatorImpl(genomeSequenceAccessor, splicingInformationContentAnnotator);

            int inserted = jannovarData.getTmByAccession().values().stream()
                    .limit(N_TRANSCRIPTS)
                    .map(splicingCalculator::calculate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(transcriptIngestDao::insertTranscript)
                    .reduce(Integer::sum)
                    .orElse(0);
            LOGGER.info("Database creation finished, inserted {} transcripts", inserted);
        }
    }

    @Test
    void makeHg38Database() throws Exception {
        JannovarData jannovarData = new JannovarDataSerializer(HG38_JANNOVAR_DB.toString()).load();
        Path dbPath = Paths.get("/home/ielis/tmp/1902_hg38_splicing_ucsc");
        DataSource dataSource = makeDataSource(dbPath);

        String locations = "classpath:db/migration";
        int migrations = applyMigrations(dataSource, locations);
        LOGGER.info("Applied {} migrations", migrations);

        Map<String, Integer> contigLengths = jannovarData.getRefDict().getContigIDToName().keySet().stream()
                .collect(Collectors.toMap(
                        id -> jannovarData.getRefDict().getContigIDToName().get(id), // key - chromosome number
                        id -> jannovarData.getRefDict().getContigIDToLength().get(id)));// value - chromosome length

        ContigIngestDao dao = new ContigIngestDao(dataSource);
        ContigIngestRunner contigIngestRunner = new ContigIngestRunner(dao, contigLengths);
        contigIngestRunner.run();

        // process PWMs
        PwmIngestDao pwm = new PwmIngestDao(dataSource);
        PwmIngestRunner pwmIngestRunner = new PwmIngestRunner(pwm, SPLICING_IC_MATRIX_PATH);
        pwmIngestRunner.run();

        // process transcripts
        try (GenomeSequenceAccessor genomeSequenceAccessor = new PrefixHandlingGenomeSequenceAccessor(HG38_FASTA_PATH, HG38_FASTA_IDX_PATH)) {
            final GenomeCoordinatesFlipper genomeCoordinatesFlipper = new GenomeCoordinatesFlipper(contigLengths);
            final TranscriptIngestDao transcriptIngestDao = new TranscriptIngestDao(dataSource, genomeCoordinatesFlipper);
            SplicingCalculator splicingCalculator = new SplicingCalculatorImpl(genomeSequenceAccessor, splicingInformationContentAnnotator);

            int inserted = jannovarData.getTmByAccession().values().stream()
                    .limit(N_TRANSCRIPTS)
                    .map(splicingCalculator::calculate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(transcriptIngestDao::insertTranscript)
                    .reduce(Integer::sum)
                    .orElse(0);
            LOGGER.info("Database creation finished, inserted {} transcripts", inserted);
        }
    }
}
