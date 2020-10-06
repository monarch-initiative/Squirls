package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.simple;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestDataSourceConfig.class)
@Disabled // todo - fix
class SimpleSplicingVariantGraphicsGeneratorTest {

    @Autowired
    private JannovarData jannovarData;

    @Autowired
    private SplicingPwmData splicingPwmData;

    private VariantAnnotator annotator;

    private SimpleSplicingVariantGraphicsGenerator generator;

    @BeforeEach
    void setUp() {
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        generator = new SimpleSplicingVariantGraphicsGenerator(splicingPwmData);
    }

    @Test
    void generateGraphics_donorPlusTwo() throws Exception {
        final SplicingVariantAlleleEvaluation evaluation = VariantsForTesting.BRCA2DonorExon15plus2QUID(jannovarData.getRefDict(), annotator);
        final String graphics = generator.generateGraphics(evaluation);

        // TODO: 9. 6. 2020 add tests
        System.out.println(graphics);
    }

    @Test
    void generateGraphics_acceptorMinusOne() throws Exception {
        final SplicingVariantAlleleEvaluation evaluation = VariantsForTesting.VWFAcceptorExon26minus2QUID(jannovarData.getRefDict(), annotator);

        final String graphics = generator.generateGraphics(evaluation);

        // TODO: 9. 6. 2020 add tests
//        System.out.println(graphics);
    }

}