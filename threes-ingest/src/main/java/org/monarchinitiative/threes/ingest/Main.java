package org.monarchinitiative.threes.ingest;

import de.charite.compbio.jannovar.data.JannovarData;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.calculators.sms.FileSMSParser;
import org.monarchinitiative.threes.core.calculators.sms.SMSParser;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.threes.ingest.config.IngestProperties;
import org.monarchinitiative.threes.ingest.pwm.PwmIngestDao;
import org.monarchinitiative.threes.ingest.pwm.PwmIngestRunner;
import org.monarchinitiative.threes.ingest.reference.ContigIngestDao;
import org.monarchinitiative.threes.ingest.reference.ContigIngestRunner;
import org.monarchinitiative.threes.ingest.reference.GenomeAssemblyDownloader;
import org.monarchinitiative.threes.ingest.septamers.SeptamerIngestDao;
import org.monarchinitiative.threes.ingest.septamers.SeptamerIngestRunner;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.threes.ingest.transcripts.SplicingCalculatorImpl;
import org.monarchinitiative.threes.ingest.transcripts.TranscriptIngestDao;
import org.monarchinitiative.threes.ingest.transcripts.TranscriptsIngestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@SpringBootApplication
public class Main implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final IngestProperties ingestProperties;

    private final JannovarData jannovarData;

    private final SplicingInformationContentCalculator splicingInformationContentAnnotator;

    public Main(IngestProperties ingestProperties, JannovarData jannovarData, SplicingInformationContentCalculator splicingInformationContentAnnotator) {
        this.ingestProperties = ingestProperties;

        // read Jannovar TODO - replace local Jannovar db with in situ building
        this.jannovarData = jannovarData;
        this.splicingInformationContentAnnotator = splicingInformationContentAnnotator;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    private static int applyMigrations(DataSource dataSource, String locations) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("SPLICING")
                .locations(locations)
                .load();
        return flyway.migrate();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Running ingest");
        try {
            GenomeAssembly assembly;
            URL genomeUrl;

            if (args.containsOption("genome-assembly")) {
                String assemblyString = args.getOptionValues("genome-assembly").get(0);
                switch (assemblyString.toUpperCase()) {
                    case "HG19":
                        assembly = GenomeAssembly.HG19;
                        genomeUrl = new URL(ingestProperties.getHg19FastaUrl());
                        break;
                    case "HG38":
                        assembly = GenomeAssembly.HG38;
                        genomeUrl = new URL(ingestProperties.getHg38FastaUrl());
                        break;
                    default:
                        LOGGER.error("Unknown assembly '{}', use one of {hg19, hg38}", assemblyString);
                        return;
                }
            } else {
                LOGGER.error("Please specify `--genome-assembly` argument");
                return;
            }

            if (!args.containsOption("version")) {
                LOGGER.error("Missing `--version` argument");
                return;
            }
            String version = args.getOptionValues("version").get(0);
            String versionedAssembly = version + "_" + assembly.getValue();
            Path genomeBuildDir = Files.createDirectories(ingestProperties.getBuildDir().resolve(versionedAssembly));

            // download files for FASTA file
            Path genomeFastaPath = genomeBuildDir.resolve(String.format("%s.fa", versionedAssembly));
            Path genomeFastaFaiPath = genomeBuildDir.resolve(String.format("%s.fa.fai", versionedAssembly));

            GenomeAssemblyDownloader downloader = new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, false);
            downloader.run();

            // where to create database file
            // TODO - for each transcript source
            JannovarTranscriptSource jannovarTranscriptSource = ingestProperties.getJannovarTranscriptSource();
            Path databasePath = genomeBuildDir.resolve(String.format("%s_splicing_%s", versionedAssembly, jannovarTranscriptSource.getValue()));
            DataSource dataSource = ingestProperties.makeDatasourceForGenome(databasePath);

            // download files for Jannovar DB build
            // TODO -

            // apply database migrations
            String locations = "classpath:db/migration";
            int migrations = applyMigrations(dataSource, locations);
            LOGGER.info("Applied {} migrations", migrations);

            ContigIngestDao contigIngestDao = new ContigIngestDao(dataSource);
            ContigIngestRunner contigIngestRunner = new ContigIngestRunner(contigIngestDao, jannovarData);
            contigIngestRunner.run();

            // process PWMs
            PwmIngestDao pwmIngestDao = new PwmIngestDao(dataSource);
            PwmIngestRunner pwmIngestRunner = new PwmIngestRunner(pwmIngestDao, ingestProperties.getSplicingInformationContentMatrixPath());
            pwmIngestRunner.run();

            // process transcripts
            try (GenomeSequenceAccessor accessor = new PrefixHandlingGenomeSequenceAccessor(genomeFastaPath, genomeFastaFaiPath)) {
                Map<String, Integer> contigLengths = jannovarData.getRefDict().getContigNameToID().keySet().stream()
                        .collect(Collectors.toMap(Function.identity(),
                                idx -> jannovarData.getRefDict().getContigIDToLength().get(jannovarData.getRefDict().getContigNameToID().get(idx))));
                GenomeCoordinatesFlipper genomeCoordinatesFlipper = new GenomeCoordinatesFlipper(contigLengths);
                TranscriptIngestDao transcriptIngestDao = new TranscriptIngestDao(dataSource, genomeCoordinatesFlipper);
                SplicingCalculator splicingCalculator = new SplicingCalculatorImpl(accessor, splicingInformationContentAnnotator);
                TranscriptsIngestRunner transcriptsIngestRunner = new TranscriptsIngestRunner(splicingCalculator, transcriptIngestDao, jannovarData);
                transcriptsIngestRunner.run();
            }

            // store septamers for SMS score
            SMSParser smsParser = new FileSMSParser(ingestProperties.getSeptamersTsvPath());
            SeptamerIngestDao septamerIngestDao = new SeptamerIngestDao(dataSource);
            SeptamerIngestRunner septamerIngestRunner = new SeptamerIngestRunner(septamerIngestDao, smsParser);
            septamerIngestRunner.run();

        } catch (Exception e) {
            LOGGER.error("Error: ", e);
            throw e;
        }
    }
}
