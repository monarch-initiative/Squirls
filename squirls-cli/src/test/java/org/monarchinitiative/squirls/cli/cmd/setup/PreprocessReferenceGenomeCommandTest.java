package org.monarchinitiative.squirls.cli.cmd.setup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreprocessReferenceGenomeCommandTest {

    private static final Path PARENT = TestDataSourceConfig.BASE_DIR.resolve("cmd").resolve("setup");

    @TempDir
    public Path tempDataDirectory;

    private final PreprocessReferenceGenomeCommand cmd = new PreprocessReferenceGenomeCommand();

    @Test
    public void call() throws Exception {
        cmd.dataDirectory = tempDataDirectory;
        cmd.genomeUrl = PARENT.resolve("shortHg19ChromFa.tar.gz").toUri().toURL();
        cmd.assemblyReportUrl = PARENT.resolve("GCF_000001405.25_GRCh37.p13_assembly_report.short.txt").toUri().toURL();

        assertFalse(Files.isRegularFile(tempDataDirectory.resolve("assembly_report.txt")));
        assertFalse(Files.isRegularFile(tempDataDirectory.resolve("genome.fa")));
        assertFalse(Files.isRegularFile(tempDataDirectory.resolve("genome.fa.fai")));
        assertFalse(Files.isRegularFile(tempDataDirectory.resolve("genome.fa.dict")));

        Integer result = cmd.call();

        assertThat(result, equalTo(0));
        assertTrue(Files.isRegularFile(tempDataDirectory.resolve("assembly_report.txt")));
        assertTrue(Files.isRegularFile(tempDataDirectory.resolve("genome.fa")));
        assertTrue(Files.isRegularFile(tempDataDirectory.resolve("genome.fa.fai")));
        assertTrue(Files.isRegularFile(tempDataDirectory.resolve("genome.fa.dict")));
    }
}