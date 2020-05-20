package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 *
 */
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class CalculatorTestBase {

    protected static final double EPSILON = 5E-4;

    @Autowired
    protected SplicingParameters splicingParameters;

    @Autowired
    protected ReferenceDictionary referenceDictionary;

    @Autowired
    protected SplicingInformationContentCalculator calculator;

    @Autowired
    protected AlleleGenerator generator;

    protected SplicingTranscript st;

    protected SequenceInterval sequenceInterval;


    @BeforeEach
    public void setUp() {
        st = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(referenceDictionary);
    }

}
