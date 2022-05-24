package org.monarchinitiative.squirls.initialize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SquirlsDataResolverTest {
    private static final Path TEST_DATA = Paths.get("src/test/resources/org/monarchinitiative/squirls/initialize");

    private SquirlsDataResolver resolver;

    @BeforeEach
    public void setUp() throws Exception {
        resolver = SquirlsDataResolver.of(TEST_DATA);
    }

    @Test
    public void getFastaStuff() {
        assertThat(resolver.genomeAssemblyReportPath(), is(TEST_DATA.resolve("assembly_report.txt")));
        assertThat(resolver.genomeFastaPath(), is(TEST_DATA.resolve("genome.fa")));
        assertThat(resolver.genomeFastaFaiPath(), is(TEST_DATA.resolve("genome.fa.fai")));
        assertThat(resolver.genomeFastaDictPath(), is(TEST_DATA.resolve("genome.fa.dict")));
    }

    @Test
    public void getDatasourcePath() {
        assertThat(resolver.dataSourcePath().toFile().getName(), is("squirls"));
    }

    @Test
    public void getPhylopPath() {
        assertThat(resolver.phylopPath().toFile().getName(), is("phylop.bw"));
    }

    @Test
    public void transcriptDatabases() {
        assertThat(resolver.ensemblSerPath(), is(TEST_DATA.resolve("tx.ensembl.ser")));
        assertThat(resolver.refseqSerPath(), is(TEST_DATA.resolve("tx.refseq.ser")));
        assertThat(resolver.ucscSerPath(), is(TEST_DATA.resolve("tx.ucsc.ser")));
    }

    @Test
    public void throwsAnExceptionWhenResourceIsMissing() {
        MissingSquirlsResourceException thrown = assertThrows(MissingSquirlsResourceException.class,
                () -> SquirlsDataResolver.of(Paths.get("src/test/resources")));
        assertThat(thrown.getMessage(), containsString("One or more files are missing in SQUIRLS directory: 'squirls.mv.db', 'tx.refseq.ser', 'tx.ensembl.ser', 'tx.ucsc.ser'"));
    }
}