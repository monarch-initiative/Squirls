package org.monarchinitiative.squirls.core.reference;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SplicingLocationDataTest {

    @Test
    public void boundariesAreNullForOutsidePosition() {
        SplicingLocationData data = SplicingLocationData.outside();
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
        assertThat(data.getExonIdx(), is(-1));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorBoundary().isEmpty(), is(true));
        assertThat(data.getAcceptorBoundary().isEmpty(), is(true));
    }
}