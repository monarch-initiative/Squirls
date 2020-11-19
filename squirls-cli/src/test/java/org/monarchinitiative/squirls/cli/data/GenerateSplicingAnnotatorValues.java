package org.monarchinitiative.squirls.cli.data;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.scoring.AGEZSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.Map;

/**
 * These tests are used to generate splicing annotator values for items in {@link VariantsForTesting}.
 */
@Disabled // these tests only work on the developer's machine
@SpringBootTest(classes = TestDataSourceConfig.class)
public class GenerateSplicingAnnotatorValues {

    //    private static final Path PHYLOP = Path.of("/Users/danisd/data/threes/hg19.100way.phyloP100way.bw");
    private static final Path PHYLOP = Path.of("/home/ielis/dub/bigwig/hg19.100way.phyloP100way.bw");
//    private static final Path PHYLOP = Path.of("hg19.100way.phyloP100way.bw");

    private static final ReferenceDictionary RD = HG19RefDictBuilder.build();
    /*
    -------------------------------------------------------
     */
    public static BigWigAccessor ACCESSOR;
    /*
    -------------------------------------------------------
    These instances contain the real values:
     */
    @Autowired
    public SplicingPwmData splicingPwmData;

    @Autowired
    @Qualifier("septamerMap")
    public Map<String, Double> septamerMap;

    @Autowired
    @Qualifier("hexamerMap")
    public Map<String, Double> hexamerMap;

    @Autowired
    public JannovarData jannovarData;

    private VariantAnnotator variantAnnotator;

    private SplicingAnnotator annotator;

    @BeforeAll
    public static void beforeAll() throws Exception {
        ACCESSOR = new BigWigAccessor(PHYLOP);
    }

    private static void printFeatureMap(Annotatable annotatable) {
        annotatable.getFeatureMap().entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.err.printf("%s=%s\n", e.getKey(), e.getValue()));
    }

    @BeforeEach
    public void setUp() {
        variantAnnotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        annotator = new AGEZSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, ACCESSOR);
    }

    @Test
    public void BRCA2DonorExon15plus2QUID() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.BRCA2DonorExon15plus2QUID(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }

    @Test
    public void ALPLDonorExon7Minus2() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.ALPLDonorExon7Minus2(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }

    @Test
    public void HBBcodingExon1UpstreamCrypticInCanonical() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.HBBcodingExon1UpstreamCrypticInCanonical(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }

    @Test
    public void HBBcodingExon1UpstreamCryptic() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.HBBcodingExon1UpstreamCryptic(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }

    @Test
    public void VWFAcceptorExon26minus2QUID() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.VWFAcceptorExon26minus2QUID(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }


    @Test
    public void TSC2AcceptorExon11Minus3() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.TSC2AcceptorExon11Minus3(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }


    @Test
    public void COL4A5AcceptorExon11Minus8() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.COL4A5AcceptorExon11Minus8(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }

    @Test
    public void RYR1codingExon102crypticAcceptor() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.RYR1codingExon102crypticAcceptor(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }


    @Test
    public void NF1codingExon9coding_SRE() throws Exception {
        WritableSplicingAllele allele = VariantsForTesting.NF1codingExon9coding_SRE(RD, variantAnnotator);
        SplicingPredictionData annotatable = annotator.annotate(allele.getPrimaryPrediction());

        printFeatureMap(annotatable);
    }
}
