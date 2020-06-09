package org.monarchinitiative.squirls.core.model;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = TestDataSourceConfig.class)
class SplicingParametersTest {


    @Autowired
    private ReferenceDictionary referenceDictionary;

    private SplicingParameters parameters;

    @BeforeEach
    void setUp() {
        parameters = SplicingParameters.builder()
                .setDonorExonic(3).setDonorIntronic(5)
                .setAcceptorIntronic(4).setAcceptorExonic(2)
                .build();
    }

    @Test
    void makeDonorRegion() {
        final GenomePosition anchor = new GenomePosition(referenceDictionary, Strand.FWD, 1, 1000);
        final GenomeInterval donor = parameters.makeDonorRegion(anchor);
        assertThat(donor, is(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 997, 1005)));
    }

    @Test
    void makeAcceptorRegion() {
        final GenomePosition anchor = new GenomePosition(referenceDictionary, Strand.FWD, 1, 1000);
        final GenomeInterval acceptor = parameters.makeAcceptorRegion(anchor);
        assertThat(acceptor, is(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 996, 1002)));
    }
}