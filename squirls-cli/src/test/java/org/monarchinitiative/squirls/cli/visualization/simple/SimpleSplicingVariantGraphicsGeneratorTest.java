package org.monarchinitiative.squirls.cli.visualization.simple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.visualization.GraphicsGeneratorTestBase;
import org.monarchinitiative.squirls.cli.visualization.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.VisualizationContextSelector;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

class SimpleSplicingVariantGraphicsGeneratorTest extends GraphicsGeneratorTestBase {

    @Mock
    public VisualizationContextSelector selector;

    private SimpleSplicingVariantGraphicsGenerator generator;

    @BeforeEach
    public void setUp() {
        super.setUp();
        generator = new SimpleSplicingVariantGraphicsGenerator(vmvtGenerator, splicingPwmData, selector);
    }

    @Test
    void generateGraphics_donorPlusTwo() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_DONOR);
        final SplicingVariantAlleleEvaluation evaluation = VariantsForTesting.BRCA2DonorExon15plus2QUID(jannovarData.getRefDict(), annotator);
        final String graphics = generator.generateGraphics(evaluation);

        // TODO: 9. 6. 2020 add tests
//        System.out.println(graphics);
    }

    @Test
    void generateGraphics_acceptorMinusOne() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_ACCEPTOR);
        final SplicingVariantAlleleEvaluation evaluation = VariantsForTesting.VWFAcceptorExon26minus2QUID(jannovarData.getRefDict(), annotator);

        final String graphics = generator.generateGraphics(evaluation);

        // TODO: 9. 6. 2020 add tests
//        System.out.println(graphics);
    }

}