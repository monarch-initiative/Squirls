package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 */
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class ScorerTestBase {

    static final double EPSILON = 0.0005;

    static SplicingTranscript st = PojosForTesting.getTranscriptWithThreeExons();

    static SequenceInterval sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons();

    @Autowired
    SplicingParameters splicingParameters;
}
