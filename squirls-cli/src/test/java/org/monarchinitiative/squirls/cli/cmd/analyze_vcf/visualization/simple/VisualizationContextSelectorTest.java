package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.simple;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class VisualizationContextSelectorTest {

    @Autowired
    public JannovarData jannovarData;

    private ReferenceDictionary rd;

    @Autowired
    public VariantAnnotator annotator;

    private VisualizationContextSelector selector;

    @BeforeEach
    public void setUp() {
        rd = jannovarData.getRefDict();
        selector = new VisualizationContextSelector();
    }

    // ****************************************** DONOR ****************************************************************

    @Test
    public void donorQuidVariant() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.BRCA2DonorExon15plus2QUID(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CANONICAL_DONOR));
    }

    @Test
    public void donorNonQuidVariantThatDisruptsTheSite() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.ALPLDonorExon7Minus2(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CANONICAL_DONOR));
    }

    @Test
    public void donorNonQuidVariantThatCreatesACrypticDonor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.HBBcodingExon1UpstreamCrypticInCanonical(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_DONOR));
    }


    @Test
    public void codingNonQuidVariantThatCreatesACrypticDonor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.HBBcodingExon1UpstreamCryptic(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_DONOR));
    }

    // ****************************************** ACCEPTOR *************************************************************

    @Test
    public void acceptorQuidVariant() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.VWFAcceptorExon26minus2QUID(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CANONICAL_ACCEPTOR));
    }

    @Test
    public void acceptorNonQuidVariantThatDisruptsTheSite() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.TSC2AcceptorExon11Minus3(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CANONICAL_ACCEPTOR));
    }

    @Test
    public void acceptorNonQuidVariantThatCreatesACrypticAcceptor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.COL4A5AcceptorExon11Minus8(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_ACCEPTOR));
    }

    @Test
    public void codingNonQuidVariantThatCreatesACrypticAcceptor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.RYR1codingExon102crypticAcceptor(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_ACCEPTOR));
    }

    // ****************************************** ESE ******************************************************************

    @Test
    public void sreVariant() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.NF1codingExon9coding_SRE(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction());
        assertThat(ctx, is(VisualizationContext.SRE));
    }
}