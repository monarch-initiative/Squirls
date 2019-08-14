package org.monarchinitiative.threes.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThreesAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    void testAllPropertiesSupplied() {
        load(ThreesAutoConfiguration.class, "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq");
        Path threesDataDirectory = context.getBean("threesDataDirectory", Path.class);
        assertThat(threesDataDirectory.getFileName(), equalTo(Paths.get("data")));

        String threesGenomeAssembly = context.getBean("threesGenomeAssembly", String.class);
        assertThat(threesGenomeAssembly, is("hg19"));

        String threesDataVersion = context.getBean("threesDataVersion", String.class);
        assertThat(threesDataVersion, is("1710"));

        String transcriptSource = context.getBean("transcriptSource", String.class);
        assertThat(transcriptSource, is("refseq"));

        // default values
        Integer maxDistanceExonUpstream = context.getBean("maxDistanceExonUpstream", Integer.class);
        assertThat(maxDistanceExonUpstream, is(50));
        Integer maxDistanceExonDownstream = context.getBean("maxDistanceExonDownstream", Integer.class);
        assertThat(maxDistanceExonDownstream, is(50));
    }

    @Test
    void testOptionalPropertiesUpstreamAndDownstreamFromExon() {
        load(ThreesAutoConfiguration.class, "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq",
                "threes.max-distance-exon-upstream=100",
                "threes.max-distance-exon-downstream=200");
        Integer maxDistanceExonUpstream = context.getBean("maxDistanceExonUpstream", Integer.class);
        assertThat(maxDistanceExonUpstream, is(100));
        Integer maxDistanceExonDownstream = context.getBean("maxDistanceExonDownstream", Integer.class);
        assertThat(maxDistanceExonDownstream, is(200));
    }

    @Test
    void testMissingDataDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Path to 3S data directory (`--threes.data-directory`) is not specified"));
    }

    @Test
    void testProvidedPathDoesNotPointToDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA + "/rocket",
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Path to 3S data directory 'src/test/resources/data/rocket' does not point to real directory"));

    }

    @Test
    void testMissingGenomeAssembly() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
//                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Genome assembly (`--threes.genome-assembly`) is not specified"));
    }

    @Test
    void testMissingDataVersion() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
//                "threes.data-version=1710",
                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Data version (`--threes.data-version`) is not specified"));
    }

    @Test
    void testMissingTranscriptSource() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710"));
//                "threes.transcript-source=refseq"));
        assertThat(thrown.getMessage(), containsString("Transcript source (`--threes.transcript-source`) is not specified"));
    }
}