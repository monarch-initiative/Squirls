package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.SimpleAnnotatable;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Random;

/**
 *
 */
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class CalculatorTestBase {

    protected static final double EPSILON = 5E-4;

    protected Random rand = new Random();

    @Autowired
    protected SplicingParameters splicingParameters;

    @Autowired
    protected ReferenceDictionary rd;

    @Autowired
    protected SplicingInformationContentCalculator calculator;

    @Autowired
    protected AlleleGenerator generator;

    @Autowired
    protected SplicingTranscriptLocator locator;

    protected SplicingTranscript st;

    protected SequenceRegion sequence;

    protected SequenceRegion sequenceOnOtherChrom;


    @BeforeEach
    public void setUp() throws Exception {
        st = PojosForTesting.getTranscriptWithThreeExons(rd);
        sequence = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        sequenceOnOtherChrom = SequenceRegion.of(new GenomeInterval(rd, Strand.FWD, 2, 0, 4), "ACGT");
        rand.setSeed(123);
    }

    protected Annotatable makeAnnotatable(GenomeVariant variant, SplicingTranscript transcript) {
        return new SimpleAnnotatable(variant,
                transcript,
                Map.of(FeatureCalculator.FASTA_TRACK_NAME, sequence,
                        FeatureCalculator.PHYLOP_TRACK_NAME, FloatRegion.of(sequence.getInterval(), getRandomScores(sequence.getInterval().length()))
                )
        );
    }

    protected float[] getRandomScores(int size) {
        final float[] scores = new float[size];
        for (int i = 0; i < size; i++) {
            scores[i] = rand.nextFloat();
        }
        return scores;
    }


    protected Annotatable makeAnnotatable(GenomeVariant variant) {
        return makeAnnotatable(variant, st);
    }

    protected Annotatable makeAnnotatable(GenomeVariant variant, SplicingTranscript transcript, SequenceRegion sequence) {
        return new SimpleAnnotatable(variant,
                transcript,
                Map.of(
                        FeatureCalculator.FASTA_TRACK_NAME, sequence,
                        FeatureCalculator.PHYLOP_TRACK_NAME, FloatRegion.of(sequence.getInterval(), getRandomScores(sequence.getInterval().length())
                        )
                )
        );
    }
}
