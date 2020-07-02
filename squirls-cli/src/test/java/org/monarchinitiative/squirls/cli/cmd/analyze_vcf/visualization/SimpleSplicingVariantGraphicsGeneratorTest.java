package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.PojosForTesting;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestDataSourceConfig.class)
class SimpleSplicingVariantGraphicsGeneratorTest {

    @Autowired
    private JannovarData jannovarData;

    @Autowired
    private SplicingParameters splicingParameters;

    private VariantAnnotator annotator;

    private SimpleSplicingVariantGraphicsGenerator generator;

    @BeforeEach
    void setUp() {
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        generator = new SimpleSplicingVariantGraphicsGenerator(new AlleleGenerator(splicingParameters));
    }

    @Test
    void generateGraphics_donorPlusFive() throws Exception {
        final SplicingVariantAlleleEvaluation evaluation = PojosForTesting.getDonorPlusFiveEvaluation(jannovarData.getRefDict(), annotator);

        final String graphics = generator.generateGraphics(evaluation).getPrimaryGraphics();

        // TODO: 9. 6. 2020 add tests
        System.out.println(graphics);
    }

    @Test
    void generateGraphics_acceptorMinusOne() throws Exception {
        final SplicingVariantAlleleEvaluation evaluation = PojosForTesting.getAcceptorMinusOneEvaluation(jannovarData.getRefDict(), annotator);

        final String graphics = generator.generateGraphics(evaluation).getPrimaryGraphics();

        // TODO: 9. 6. 2020 add tests
        System.out.println(graphics);
    }

}