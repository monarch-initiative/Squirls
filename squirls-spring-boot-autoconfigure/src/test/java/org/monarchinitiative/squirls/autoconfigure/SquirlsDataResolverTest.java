package org.monarchinitiative.squirls.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SquirlsDataResolverTest {
    private static final Path TEST_DATA = Paths.get("src/test/resources/data");

    private SquirlsDataResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new SquirlsDataResolver(TEST_DATA, "1710", "hg19");
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
        assertThat(resolver.dataSourcePath().toFile().getName(), is("1710_hg19.splicing"));
    }

    @Test
    public void getPhylopPath() {
        assertThat(resolver.phylopPath().toFile().getName(), is("1710_hg19.phylop.bw"));
    }

    @Test
    public void throwsAnExceptionWhenResourceIsMissing() {
        final MissingSquirlsResourceException thrown = assertThrows(MissingSquirlsResourceException.class, () -> new SquirlsDataResolver(Paths.get("src/test/resources"), "1710", "hg19"));
        assertThat(thrown.getMessage(), containsString("The file `1710_hg19.fa` is missing in SQUIRLS directory"));
    }
}