package org.monarchinitiative.threes.ingest.septamers;

import org.monarchinitiative.threes.core.calculators.sms.SMSParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SeptamerIngestRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeptamerIngestRunner.class);

    private final SeptamerIngestDao dao;

    private final SMSParser parser;

    public SeptamerIngestRunner(SeptamerIngestDao dao, SMSParser parser) {
        this.dao = dao;
        this.parser = parser;
    }

    @Override
    public void run() {
        LOGGER.info("Inserting septamers into the database");
        int nRows = dao.insertSeptamers(parser.getSeptamerMap());
        LOGGER.info("Inserted {} septamer entries", nRows);

    }
}
