package org.monarchinitiative.sss.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class MainIngestRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainIngestRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Running ingest");
    }
}
