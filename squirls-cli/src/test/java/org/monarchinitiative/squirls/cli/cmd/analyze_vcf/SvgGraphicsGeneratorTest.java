package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.PojosForTesting;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestDataSourceConfig.class)
class SvgGraphicsGeneratorTest {

    @Autowired
    private JannovarData jannovarData;

    private VariantAnnotator annotator;

    private SvgGraphicsGenerator generator;

    @BeforeEach
    void setUp() {
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        generator = new SvgGraphicsGenerator();
    }

    @Test
    void generate() throws Exception {
        final SplicingVariantAlleleEvaluation evaluation = PojosForTesting.getDonorPlusFiveEvaluation(jannovarData.getRefDict(), annotator);
        final String graphics = generator.generateGraphics(evaluation);

        // TODO: 9. 6. 2020 add tests
    }
}