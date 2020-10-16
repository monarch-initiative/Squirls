package org.monarchinitiative.squirls.cli.visualization.simple;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.cli.visualization.SimpleVisualizationContextSelector;
import org.monarchinitiative.squirls.cli.visualization.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.VisualizationContextSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = TestDataSourceConfig.class)
class VisualizationContextSelectorTest {

    @Autowired
    private JannovarData jannovarData;

    private ReferenceDictionary rd;

    private VariantAnnotator annotator;

    private VisualizationContextSelector selector;

    @BeforeEach
    void setUp() {
        rd = jannovarData.getRefDict();
        annotator = new VariantAnnotator(rd, jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        selector = new SimpleVisualizationContextSelector();
    }

    // ****************************************** DONOR ****************************************************************

    @Test
    void donorQuidVariant() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.BRCA2DonorExon15plus2QUID(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_DONOR));
    }

    @Test
    void donorNonQuidVariantThatDisruptsTheSite() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.ALPLDonorExon7Minus2(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_DONOR));
    }

    @Test
    void donorNonQuidVariantThatCreatesACrypticDonor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.HBBcodingExon1UpstreamCrypticInCanonical(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_DONOR));
    }


    @Test
    void codingNonQuidVariantThatCreatesACrypticDonor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.HBBcodingExon1UpstreamCryptic(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_DONOR));
    }

    // ****************************************** ACCEPTOR *************************************************************

    @Test
    void acceptorQuidVariant() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.VWFAcceptorExon26minus2QUID(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_ACCEPTOR));
    }

    @Test
    void acceptorNonQuidVariantThatDisruptsTheSite() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.TSC2AcceptorExon11Minus3(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CANONICAL_ACCEPTOR));
    }

    @Test
    void acceptorNonQuidVariantThatCreatesACrypticAcceptor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.COL4A5AcceptorExon11Minus8(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_ACCEPTOR));
    }

    @Test
    void codingNonQuidVariantThatCreatesACrypticAcceptor() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.RYR1codingExon102crypticAcceptor(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.CRYPTIC_ACCEPTOR));
    }

    // ****************************************** ESE ******************************************************************

    @Test
    void sreVariant() throws Exception {
        final SplicingVariantAlleleEvaluation ve = VariantsForTesting.NF1codingExon9coding_SRE(rd, annotator);
        final VisualizationContext ctx = selector.selectContext(ve.getPrimaryPrediction().getFeatureMap());
        assertThat(ctx, is(VisualizationContext.SRE));
    }
}