package org.monarchinitiative.threes.autoconfigure;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.VariantSplicingEvaluator;
import org.monarchinitiative.threes.core.data.SplicingTranscriptSource;
import org.monarchinitiative.threes.core.scoring.SplicingAnnotator;
import org.springframework.beans.factory.BeanCreationException;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThreesAutoConfigurationTest extends AbstractAutoConfigurationTest {

    /**
     * Small bigWig file containing phyloP scores for region chr9:100,000-101,000 (0-based).
     */
    private static final Path SMALL_BW = TEST_DATA.getParent().resolve("small.bw");

    /**
     * Test how the normal configuration should look like and beans that should be available
     */
    @Test
    void testAllPropertiesSupplied() {
        load(ThreesAutoConfiguration.class, "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.phylop-bigwig-path=" + SMALL_BW);
        /*
         * Data we expect to get from the user
         */
        Path threesDataDirectory = context.getBean("threesDataDirectory", Path.class);
        assertThat(threesDataDirectory.getFileName(), equalTo(Paths.get("data")));

        String threesGenomeAssembly = context.getBean("threesGenomeAssembly", String.class);
        assertThat(threesGenomeAssembly, is("hg19"));

        String threesDataVersion = context.getBean("threesDataVersion", String.class);
        assertThat(threesDataVersion, is("1710"));

        Path phylopBigwigPath = context.getBean("phylopBigwigPath", Path.class);
        assertThat(phylopBigwigPath, is(SMALL_BW));

        /*
         * Optional - default values
         */
        ThreesProperties properties = context.getBean(ThreesProperties.class);
        assertThat(properties.getClassifier().getVersion(), is("v1"));

        /*
         * High-level beans
         */
        GenomeSequenceAccessor genomeSequenceAccessor = context.getBean("genomeSequenceAccessor", GenomeSequenceAccessor.class);
        assertThat(genomeSequenceAccessor, is(notNullValue()));

        SplicingTranscriptSource splicingTranscriptSource = context.getBean("splicingTranscriptSource", SplicingTranscriptSource.class);
        assertThat(splicingTranscriptSource, is(notNullValue()));

        SplicingAnnotator splicingAnnotator = context.getBean("splicingAnnotator", SplicingAnnotator.class);
        assertThat(splicingAnnotator, is(notNullValue()));

        VariantSplicingEvaluator evaluator = context.getBean("variantSplicingEvaluator", VariantSplicingEvaluator.class);
        assertThat(evaluator, is(notNullValue()));
    }

    @Test
    void testOptionalProperties() {
        load(ThreesAutoConfiguration.class, "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.phylop-bigwig-path=" + SMALL_BW,
                "threes.classifier.version=v1.1",
                "threes.classifier.max-variant-length=50");

        ThreesProperties properties = context.getBean(ThreesProperties.class);
        assertThat(properties.getClassifier().getVersion(), is("v1.1"));
        assertThat(properties.getClassifier().getMaxVariantLength(), is(50));
    }

    @Test
    void testMissingDataDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Path to 3S data directory (`--threes.data-directory`) is not specified"));
    }

    @Test
    void testProvidedPathDoesNotPointToDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA + "/rocket",
                "threes.genome-assembly=hg19",
                "threes.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Path to 3S data directory 'src/test/resources/data/rocket' does not point to real directory"));

    }

    @Test
    void testMissingGenomeAssembly() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
//                "threes.genome-assembly=hg19",
                "threes.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Genome assembly (`--threes.genome-assembly`) is not specified"));
    }

    @Test
    void testMissingDataVersion() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19"
//                "threes.data-version=1710",
        ));
        assertThat(thrown.getMessage(), containsString("Data version (`--threes.data-version`) is not specified"));
    }

    @Test
    void testMissingPhylopPath() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710"
        ));
        assertThat(thrown.getMessage(), containsString("Path to PhyloP bigwig file is not specified"));
    }

    @Test
    void testNonExistingClassifier() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(ThreesAutoConfiguration.class,
                "threes.data-directory=" + TEST_DATA,
                "threes.genome-assembly=hg19",
                "threes.data-version=1710",
                "threes.phylop-bigwig-path=" + SMALL_BW,
                "threes.classifier.version=puddle"));
        assertThat(thrown.getMessage(), containsString("Classifier version `puddle` is not available, choose one from "));
    }
}