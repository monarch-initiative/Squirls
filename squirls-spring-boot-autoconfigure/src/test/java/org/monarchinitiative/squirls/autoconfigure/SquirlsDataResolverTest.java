package org.monarchinitiative.squirls.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class SquirlsDataResolverTest {
    private static final Path TEST_DATA = Paths.get("src/test/resources/data");

    private SquirlsDataResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SquirlsDataResolver(TEST_DATA, "1710", "hg19");
    }

    @Test
    void getDatasourcePath() {
        assertThat(resolver.getDatasourcePath().toFile().getName(), is("1710_hg19_splicing"));
    }
}