package org.monarchinitiative.squirls.core.data;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class IntronPropertiesTest {

    @Test
    public void parsePayload() {
        String payload = "DONOR=8.429;ACCEPTOR=4.541";
        final IntronProperties ip = IntronProperties.parseString(payload);

        assertThat(ip.getDonorScore(), is(8.429));
        assertThat(ip.getAcceptorScore(), is(4.541));

    }
}