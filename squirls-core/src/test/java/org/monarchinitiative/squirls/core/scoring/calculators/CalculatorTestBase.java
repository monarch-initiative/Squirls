package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.monarchinitiative.squirls.core.PojosForTesting;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 *
 */
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class CalculatorTestBase {

    public static final double EPSILON = 5E-4;

    @Autowired
    public SplicingParameters splicingParameters;

    @Autowired
    public ReferenceDictionary rd;

    @Autowired
    public SplicingInformationContentCalculator calculator;

    @Autowired
    public AlleleGenerator generator;

    public SplicingTranscript st;

    @Autowired
    public SplicingTranscriptLocator locator;

    protected SplicingTranscript st;

    public SequenceInterval sequenceInterval;

    protected SequenceInterval sequenceOnOtherChrom;


    @BeforeEach
    public void setUp() throws Exception {
        st = PojosForTesting.getTranscriptWithThreeExons(rd);
        sequenceInterval = PojosForTesting.getSequenceIntervalForTranscriptWithThreeExons(rd);
        sequenceOnOtherChrom = SequenceInterval.builder()
                .interval(new GenomeInterval(rd, Strand.FWD, 2, 0, 4))
                .sequence("ACGT")
                .build();
    }

}
