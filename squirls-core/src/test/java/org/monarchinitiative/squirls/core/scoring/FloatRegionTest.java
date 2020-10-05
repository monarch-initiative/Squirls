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

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class FloatRegionTest {


    @Autowired
    public ReferenceDictionary rd;

    private FloatRegion region;

    @BeforeEach
    public void setUp() {
        final GenomeInterval interval = new GenomeInterval(rd, Strand.FWD, 1, 10, 15);
        final List<Float> floats = List.of(.5f, .6f, .7f, .8f, .9f);
        region = FloatRegion.of(interval, floats);
    }

    @Test
    public void getValuesForInterval() {
        GenomeInterval other = new GenomeInterval(rd, Strand.FWD, 1, 10, 13);
        List<Float> values = region.getValuesForInterval(other);

        assertThat(values, hasSize(3));
        assertThat(values, hasItems(.5f, .6f, .7f));

        other = new GenomeInterval(rd, Strand.FWD, 1, 10, 15);
        values = region.getValuesForInterval(other);

        assertThat(values, hasSize(5));
        assertThat(values, hasItems(.5f, .6f, .7f, .8f, .9f));
    }


    @Test
    public void getValuesForIntervalOppositeStrand() {
        final GenomeInterval other = new GenomeInterval(rd, Strand.FWD, 1, 10, 13).withStrand(Strand.REV);
        final List<Float> values = region.getValuesForInterval(other);

        assertThat(values, hasSize(3));
        assertThat(values, hasItems(.7f, .6f, .5f));
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
        List<Float> values = region.getValuesForInterval(other);
        assertThat(values, is(empty()));

    }
}