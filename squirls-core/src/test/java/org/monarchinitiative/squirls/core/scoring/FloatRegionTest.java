package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class FloatRegionTest {


    @Autowired
    public ReferenceDictionary rd;

    private FloatRegion region;

    @BeforeEach
    public void setUp() {
        final GenomeInterval interval = new GenomeInterval(rd, Strand.FWD, 1, 10, 15);
        region = FloatRegion.of(interval, new float[]{.5f, .6f, .7f, .8f, .9f});
    }

    @Test
    public void getValuesForInterval() {
        GenomeInterval other = new GenomeInterval(rd, Strand.FWD, 1, 10, 13);
        float[] values = region.getValuesForInterval(other);

        assertThat(values.length, is(3));
        assertThat(values, is(new float[]{.5f, .6f, .7f}));

        other = new GenomeInterval(rd, Strand.FWD, 1, 10, 15);
        values = region.getValuesForInterval(other);

        assertThat(values.length, is(5));
        assertThat(values, is(new float[]{.5f, .6f, .7f, .8f, .9f}));
    }


    @Test
    public void getValuesForIntervalOppositeStrand() {
        final GenomeInterval other = new GenomeInterval(rd, Strand.FWD, 1, 10, 13).withStrand(Strand.REV);
        float[] values = region.getValuesForInterval(other);

        assertThat(values.length, is(3));
        assertThat(values, is(new float[]{.7f, .6f, .5f}));
    }

    @CsvSource({
            "9,15,FWD",
            "10,16,FWD",
            "9,15,REV",
            "10,16,REV"
    })
    @ParameterizedTest
    public void getValuesForInvalidInput(int begin, int end, String strand) {
        GenomeInterval other = new GenomeInterval(rd, Strand.FWD, 1, begin, end).withStrand(Strand.valueOf(strand));
        float[] values = region.getValuesForInterval(other);
        assertThat(values, is(new float[0]));

    }
}