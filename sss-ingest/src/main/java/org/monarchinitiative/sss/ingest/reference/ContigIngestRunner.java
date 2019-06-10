package org.monarchinitiative.sss.ingest.reference;

import de.charite.compbio.jannovar.data.JannovarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        final Map<String, Integer> contigLengths = jannovarData.getRefDict().getContigNameToID().keySet().stream()
                .collect(Collectors.toMap(Function.identity(),
                        idx -> jannovarData.getRefDict().getContigIDToLength().get(jannovarData.getRefDict().getContigNameToID().get(idx))));
        Integer inserted = contigLengths.entrySet().stream()
                .map(e -> dao.insertContig(e.getKey(), e.getValue()))
                .reduce(Integer::sum)
                .orElse(0);
        LOGGER.info("Stored lengths of {} contigs", inserted);
    }
}
