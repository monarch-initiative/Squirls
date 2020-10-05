package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.junit.jupiter.api.BeforeEach;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class SplicingAnnotatorTestBase {

    protected static final double EPSILON = 0.0005;

    @Autowired
    public ReferenceDictionary rd;

    @Autowired
    public SplicingPwmData splicingPwmData;

    @Qualifier("hexamerMap")
    @Autowired
    public Map<String, Double> hexamerMap;

    @Qualifier("septamerMap")
    @Autowired
    public Map<String, Double> septamerMap;

    private Map<String, TrackRegion<?>> trackMap;

    protected SplicingTranscript st;

    protected SequenceInterval sequence;

    @BeforeEach
    public void setUp() {
        st = PojosForTesting.getTranscriptWithThreeExons(rd);
        sequence = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        trackMap = PojosForTesting.getTrackMap(rd);
    }

    protected Annotatable makeAnnotatable(GenomeVariant variant) {
        return new SimpleAnnotatable(variant, st, trackMap);
    }
}
