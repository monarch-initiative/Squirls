package org.monarchinitiative.squirls.autoconfigure;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.data.SplicingTranscriptSource;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.springframework.beans.factory.BeanCreationException;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SquirlsAutoConfigurationTest extends AbstractAutoConfigurationTest {

    /**
     * Small bigWig file containing phyloP scores for region chr9:100,000-101,000 (0-based).
     */
    private static final Path SMALL_BW = TEST_DATA.getParent().resolve("small.bw");

    /**
     * Test how the normal configuration should look like and beans that should be available
     */
    @Test
    void testAllPropertiesSupplied() {
        load(SquirlsAutoConfiguration.class, "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.phylop-bigwig-path=" + SMALL_BW);
        /*
         * Data we expect to get from the user
         */
        Path threesDataDirectory = context.getBean("squirlsDataDirectory", Path.class);
        assertThat(threesDataDirectory.getFileName(), equalTo(Paths.get("data")));

        String squirlsGenomeAssembly = context.getBean("squirlsGenomeAssembly", String.class);
        assertThat(squirlsGenomeAssembly, is("hg19"));

        String squirlsDataVersion = context.getBean("squirlsDataVersion", String.class);
        assertThat(squirlsDataVersion, is("1710"));

        Path phylopBigwigPath = context.getBean("phylopBigwigPath", Path.class);
        assertThat(phylopBigwigPath, is(SMALL_BW));

        /*
         * Optional - default values
         */
        SquirlsProperties properties = context.getBean(SquirlsProperties.class);
        assertThat(properties.getClassifier().getVersion(), is("v0.4.1"));

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
        load(SquirlsAutoConfiguration.class, "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.phylop-bigwig-path=" + SMALL_BW,
                "squirls.classifier.version=v1.1",
                "squirls.classifier.max-variant-length=50",
                "squirls.annotator.version=agez"
        );

        SquirlsProperties properties = context.getBean(SquirlsProperties.class);
        assertThat(properties.getClassifier().getVersion(), is("v1.1"));
        assertThat(properties.getClassifier().getMaxVariantLength(), is(50));
        assertThat(properties.getAnnotator().getVersion(), is("agez"));
    }

    @Test
    void testMissingDataDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Path to Squirls data directory (`--squirls.data-directory`) is not specified"));
    }

    @Test
    void testProvidedPathDoesNotPointToDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA + "/rocket",
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Path to Squirls data directory 'src/test/resources/data/rocket' does not point to real directory"));

    }

    @Test
    void testMissingGenomeAssembly() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
//                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Genome assembly (`--squirls.genome-assembly`) is not specified"));
    }

    @Test
    void testMissingDataVersion() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19"
//                "squirls.data-version=1710",
        ));
        assertThat(thrown.getMessage(), containsString("Data version (`--squirls.data-version`) is not specified"));
    }

    @Test
    void testMissingPhylopPath() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"
        ));
        assertThat(thrown.getMessage(), containsString("Path to PhyloP bigwig file is not specified"));
    }

    @Test
    void testNonExistingClassifier() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.phylop-bigwig-path=" + SMALL_BW,
                "squirls.classifier.version=puddle"));
        assertThat(thrown.getMessage(), containsString("Classifier version `puddle` is not available, choose one from "));
    }

    @Test
    void testNonExistingSplicingAnnotator() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.phylop-bigwig-path=" + SMALL_BW,
                "squirls.annotator.version=non-existing"));
        assertThat(thrown.getMessage(), containsString("invalid 'squirls.annotator.version' property value: `non-existing`"));
    }
}