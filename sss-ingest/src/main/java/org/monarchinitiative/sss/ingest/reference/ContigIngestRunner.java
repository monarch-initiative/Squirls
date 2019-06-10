package org.monarchinitiative.sss.ingest.reference;

import de.charite.compbio.jannovar.data.JannovarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ContigIngestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContigIngestRunner.class);

    private final ContigIngestDao dao;

    private final JannovarData jannovarData;

    public ContigIngestRunner(ContigIngestDao dao, JannovarData jannovarData) {
        this.dao = dao;
        this.jannovarData = jannovarData;
    }

    public void run() {
        LOGGER.info("Storing lengths of {} contigs", jannovarData.getRefDict().getContigIDToLength().size());
        Integer inserted = jannovarData.getRefDict().getContigIDToLength().entrySet().stream()
                .map(e -> dao.insertContig(jannovarData.getRefDict().getContigIDToName().get(e.getKey()), e.getValue()))
                .reduce(Integer::sum)
                .orElse(0);
        LOGGER.info("Stored {} contigs", inserted);
    }
}
