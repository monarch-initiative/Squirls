package org.monarchinitiative.threes.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class ThreesDataResolverTest {
    private static final Path TEST_DATA = Paths.get("src/test/resources/data");

    private ThreesDataResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ThreesDataResolver(TEST_DATA, "1710", "hg19");
    }

    @Test
    void getFastaStuff() {
        final Path versionedAssemblyDataDirPath = TEST_DATA.resolve("1710_hg19");
        assertThat(resolver.genomeFastaPath(), is(versionedAssemblyDataDirPath.resolve("1710_hg19.fa")));
        assertThat(resolver.genomeFastaFaiPath(), is(versionedAssemblyDataDirPath.resolve("1710_hg19.fa.fai")));
        assertThat(resolver.genomeFastaDictPath(), is(versionedAssemblyDataDirPath.resolve("1710_hg19.fa.dict")));
    }

    @Test
    void getDatasourcePath() {
        assertThat(resolver.getDatasourcePath().toFile().getName(), is("1710_hg19_splicing"));
    }
}