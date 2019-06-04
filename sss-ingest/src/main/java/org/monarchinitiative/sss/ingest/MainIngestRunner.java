package org.monarchinitiative.sss.ingest;

import de.charite.compbio.jannovar.data.JannovarData;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.sss.ingest.config.IngestProperties;
import org.monarchinitiative.sss.ingest.transcripts.IngestDao;
import org.monarchinitiative.sss.ingest.transcripts.SplicingCalculator;
import org.monarchinitiative.sss.ingest.transcripts.UniversalIngestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

/**
 *
 */
@Component
public class MainIngestRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainIngestRunner.class);

    private final IngestProperties ingestProperties;

    private final DataSource dataSource;

    private final JannovarData jannovarData;

    private final SplicingCalculator splicingCalculator;

    public MainIngestRunner(IngestProperties ingestProperties,
                            DataSource dataSource,
                            JannovarData jannovarData, SplicingCalculator splicingCalculator) {
        this.ingestProperties = ingestProperties;
        this.dataSource = dataSource;

        // read Jannovar TODO - replace local Jannovar db with in situ building
        this.jannovarData = jannovarData;
        this.splicingCalculator = splicingCalculator;
    }

    private static int applyMigrations(DataSource dataSource, String locations) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("SPLICING")
                .locations(locations)
                .load();
        flyway.clean();
        return flyway.migrate();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Running ingest");
        try {
            GenomeAssembly ga = ingestProperties.getGenomeAssembly();

            // download files for Jannovar DB build

            // download files for FASTA file

            // apply database migrations
            String locations = "classpath:db/migration";
            int migrations = applyMigrations(dataSource, locations);
            LOGGER.info("Applied {} migrations", migrations);

            JannovarTranscriptSource jts = ingestProperties.getJannovarTranscriptSource();

            IngestDao ingestDao = new UniversalIngestDao(dataSource);

            // process transcripts
            LOGGER.info("Processing {} transcripts", jannovarData.getTmByAccession().values().size());

            int inserted = jannovarData.getTmByAccession().values().stream()
                    .map(splicingCalculator::calculate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(ingestDao::insertTranscript)
                    .reduce(Integer::sum)
                    .orElse(0);
            LOGGER.info("Inserted {} transcripts", inserted);

        } catch (Exception e) {
            LOGGER.error("Error: ", e);
        }
    }
}
