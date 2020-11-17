package org.monarchinitiative.squirls.cli.visualization.selector;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class SimpleVisualizationContextSelectorTest {

    @Autowired
    public JannovarData jannovarData;

    private ReferenceDictionary rd;

    @Autowired
    public VariantAnnotator annotator;

    private SimpleVisualizationContextSelector selector;

    @BeforeEach
    public void setUp() {
        rd = jannovarData.getRefDict();
        selector = new SimpleVisualizationContextSelector();
    }

    // ****************************************** DONOR ****************************************************************

    @Test
    public void donorQuidVariant() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.BRCA2DonorExon15plus2QUID(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_DONOR));
    }

    @Test
    public void donorNonQuidVariantThatDisruptsTheSite() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.ALPLDonorExon7Minus2(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_DONOR));
    }

    @Test
    public void donorNonQuidVariantThatCreatesACrypticDonor() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.HBBcodingExon1UpstreamCrypticInCanonical(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_DONOR));
    }


    @Test
    public void codingNonQuidVariantThatCreatesACrypticDonor() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.HBBcodingExon1UpstreamCryptic(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_DONOR));
    }

    // ****************************************** ACCEPTOR *************************************************************

    @Test
    public void acceptorQuidVariant() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.VWFAcceptorExon26minus2QUID(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_ACCEPTOR));
    }

    @Test
    public void acceptorNonQuidVariantThatDisruptsTheSite() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.TSC2AcceptorExon11Minus3(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_ACCEPTOR));
    }

    @Test
    public void acceptorNonQuidVariantThatCreatesACrypticAcceptor() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.COL4A5AcceptorExon11Minus8(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_ACCEPTOR));
    }

    @Test
    @Disabled // TODO: 17. 11. 2020 check
    public void codingNonQuidVariantThatCreatesACrypticAcceptor() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.RYR1codingExon102crypticAcceptor(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_ACCEPTOR));
    }

    // ****************************************** ESE ******************************************************************

    @Test
    public void sreVariant() throws Exception {
        final WritableSplicingAllele ve = VariantsForTesting.NF1codingExon9coding_SRE(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.SRE));
    }
}