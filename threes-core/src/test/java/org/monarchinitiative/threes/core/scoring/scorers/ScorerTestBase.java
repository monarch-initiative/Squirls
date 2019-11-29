package org.monarchinitiative.threes.core.scoring.scorers;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 *
 */
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class ScorerTestBase {

    static final double EPSILON = 0.0005;
    @Autowired
    SplicingParameters splicingParameters;

    @Autowired
    protected ReferenceDictionary referenceDictionary;

    protected SplicingTranscript st;

    protected SequenceInterval sequenceInterval;


    @BeforeEach
    void setUp() {
        st = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary);
    }

}
