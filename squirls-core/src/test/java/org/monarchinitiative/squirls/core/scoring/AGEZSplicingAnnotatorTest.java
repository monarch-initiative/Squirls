package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class AGEZSplicingAnnotatorTest {

    private static final double EPSILON = 0.0005;

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

    @Mock
    public BigWigAccessor accessor;

    private SplicingTranscript st;

    private SequenceInterval sequence;

    private AGEZSplicingAnnotator annotator;


    @BeforeEach
    public void setUp() {
        st = PojosForTesting.getTranscriptWithThreeExons(rd);
        sequence = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        annotator = new AGEZSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, accessor);
    }

    @Test
    public void annotate() {
        // "1391,c,cag,1.", // match, turns "c" -> "cag" within AGEZ
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(rd, Strand.FWD, 1, 1391), "c", "cag");

        SimpleAnnotatable ann = new SimpleAnnotatable(variant, st, sequence);
        ann = annotator.annotate(ann);

        assertThat(ann.getFeature("creates_ag_in_agez", Double.class), is(closeTo(1., EPSILON)));
        assertThat(ann.getFeature("ppt_is_truncated", Double.class), is(closeTo(0., EPSILON)));
    }
}