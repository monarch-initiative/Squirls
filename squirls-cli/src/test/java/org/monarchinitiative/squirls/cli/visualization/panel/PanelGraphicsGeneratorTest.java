package org.monarchinitiative.squirls.cli.visualization.panel;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.visualization.GraphicsGeneratorTestBase;
import org.monarchinitiative.squirls.cli.visualization.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.VisualizationContextSelector;

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
        final String content = generator.generateGraphics(VariantsForTesting.BRCA2DonorExon15plus2QUID(jannovarData.getRefDict(), annotator));

        System.err.println(content);
    }

    @Test
    @Disabled
    public void canonicalDonorExtremeCase() {
        // this case represents variant `chr3:10088407AG>A` that deletes G from GT in the donor site
        // TODO: 20. 10. 2020 make sure that the VMVT code works
        final String svg = vmvtGenerator.getDonorDistributionSvg("TTAgtaagt", "TTAtaagtg");
        System.out.println(svg);
    }

    @Test
    public void canonicalAcceptor() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_ACCEPTOR);
        final String content = generator.generateGraphics(VariantsForTesting.TSC2AcceptorExon11Minus3(jannovarData.getRefDict(), annotator));

        System.err.println(content);
    }
}