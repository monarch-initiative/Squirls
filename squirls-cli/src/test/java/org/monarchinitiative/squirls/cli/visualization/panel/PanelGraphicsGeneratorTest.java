package org.monarchinitiative.squirls.cli.visualization.panel;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.visualization.GraphicsGeneratorTestBase;
import org.monarchinitiative.squirls.cli.visualization.VisualizableVariantAllele;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContextSelector;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;

import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.when;

public class PanelGraphicsGeneratorTest extends GraphicsGeneratorTestBase {

    @Mock
    public VisualizationContextSelector selector;

    private PanelGraphicsGenerator generator;

    @BeforeEach
    public void setUp() {
        super.setUp();
        generator = new PanelGraphicsGenerator(vmvtGenerator, splicingPwmData, selector);
    }

    @Test
    public void canonicalDonor() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_DONOR);

        WritableSplicingAllele writableSplicingAllele = VariantsForTesting.BRCA2DonorExon15plus2QUID(jannovarData.getRefDict(), annotator);
        VisualizableVariantAllele allele = toVisualizableAllele(writableSplicingAllele);

        String content = generator.generateGraphics(allele);

        System.err.println(content);
    }

    @Disabled
    @Test
    public void canonicalDonorExtremeCase() {
        // this case represents variant `chr3:10088407AG>A` that deletes G from GT in the donor site
        // the test might be discarded if is not necessary in future
        final String svg = vmvtGenerator.getDonorDistributionSvg("TTAgtaagt", "TTAtaagtg");
        System.out.println(svg);
    }

    @Test
    public void canonicalAcceptor() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_ACCEPTOR);
        WritableSplicingAllele writableSplicingAllele = VariantsForTesting.TSC2AcceptorExon11Minus3(jannovarData.getRefDict(), annotator);
        VisualizableVariantAllele allele = toVisualizableAllele(writableSplicingAllele);
        final String content = generator.generateGraphics(allele);

        System.err.println(content);
    }
}