package org.monarchinitiative.threes.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ThreeSAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    void basic() {
        assertThat(true, is(true));
    }

    @Configuration
    @ImportAutoConfiguration(value = ThreeSAutoConfiguration.class)
    protected static class EmptyConfiguration {
        // has to be at least "protected" no-op
    }
}