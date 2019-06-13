package org.monarchinitiative.threes.cli;


import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.transcript.SplicingTranscriptLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class ExampleRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleRunner.class);

    private final SplicingTranscriptSource splicingTranscriptSource;

    private final SplicingTranscriptLocator splicingTranscriptLocator;

    public ExampleRunner(SplicingTranscriptLocator splicingTranscriptLocator, SplicingTranscriptSource splicingTranscriptSource) {
        this.splicingTranscriptLocator = splicingTranscriptLocator;
        this.splicingTranscriptSource = splicingTranscriptSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            final List<SplicingTranscript> transcripts = splicingTranscriptSource.fetchTranscripts("4", 7_000_000, 10_000_000);
            transcripts.forEach(System.out::println);
        } catch (Exception e) {
            LOGGER.error("Exception ", e);
        }
    }
}
