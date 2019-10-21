package org.monarchinitiative.threes.ingest.reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class ContigIngestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContigIngestRunner.class);

    private final ContigIngestDao dao;

    private final Map<String, Integer> contigLengths;


    public ContigIngestRunner(ContigIngestDao dao, Map<String, Integer> contigLengths) {
        this.dao = dao;
        this.contigLengths = contigLengths;
    }

    public void run() {
        Integer inserted = contigLengths.entrySet().stream()
                .map(e -> dao.insertContig(e.getKey(), e.getValue()))
                .reduce(Integer::sum)
                .orElse(0);
        LOGGER.info("Stored lengths of {} contigs", inserted);
    }
}
