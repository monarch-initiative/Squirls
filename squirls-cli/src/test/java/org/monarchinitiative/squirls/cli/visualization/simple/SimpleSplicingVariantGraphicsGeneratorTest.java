package org.monarchinitiative.squirls.cli.visualization.simple;

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
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@Disabled // the generator is likely to be deprecated soon
public class SimpleSplicingVariantGraphicsGeneratorTest extends GraphicsGeneratorTestBase {

    @Mock
    public VisualizationContextSelector selector;

    @Mock
    public GenomeSequenceAccessor accessor;

    private SimpleSplicingVariantGraphicsGenerator generator;

    @BeforeEach
    public void setUp() {
        super.setUp();
        generator = new SimpleSplicingVariantGraphicsGenerator(vmvtGenerator, splicingPwmData, selector, accessor);
    }

    @Test
    public void generateGraphics_donorPlusTwo() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_DONOR);
        when(accessor.fetchSequence(any())).thenReturn(Optional.empty()); // TODO: 17. 11. 2020 fix if necessary

        WritableSplicingAllele evaluation = VariantsForTesting.BRCA2DonorExon15plus2QUID(jannovarData.getRefDict(), annotator);
        VisualizableVariantAllele allele = toVisualizableAllele(evaluation);
        final String graphics = generator.generateGraphics(allele);

        // TODO: 9. 6. 2020 add tests
//        System.out.println(graphics);
    }

    @Test
    public void generateGraphics_acceptorMinusOne() throws Exception {
        when(selector.selectContext(anyMap())).thenReturn(VisualizationContext.CANONICAL_ACCEPTOR);
        when(accessor.fetchSequence(any())).thenReturn(Optional.empty()); // TODO: 17. 11. 2020 fix if necessary

        WritableSplicingAllele evaluation = VariantsForTesting.VWFAcceptorExon26minus2QUID(jannovarData.getRefDict(), annotator);
        VisualizableVariantAllele allele = toVisualizableAllele(evaluation);

        final String graphics = generator.generateGraphics(allele);

        // TODO: 9. 6. 2020 add tests
//        System.out.println(graphics);
    }

}