package org.monarchinitiative.squirls.cli.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.scoring.AGEZSplicingAnnotator;
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

    private static final Path PHYLOP = Path.of("/Users/danisd/data/threes/hg19.100way.phyloP100way.bw");

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

    private SplicingAnnotator annotator;

    @BeforeAll
    public static void beforeAll() throws Exception {
        ACCESSOR = new BigWigAccessor(PHYLOP);
    }

    @BeforeEach
    public void setUp() {
        annotator = new AGEZSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, ACCESSOR);
    }

    @Test
    public void annotateBRCA2Variant() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 13, 32_930_748, PositionType.ONE_BASED),
                "T", "G");

        final SimpleAnnotatable ann = new SimpleAnnotatable(variant, Transcripts.brca2Transcripts(RD).get(0), Sequences.getBrca2Exon15Sequence(RD));

        final SimpleAnnotatable annotate = annotator.annotate(ann);
        annotate.getFeatureMap().entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.err.printf("%s=%s\n", e.getKey(), e.getValue()));
    }

    @Test
    public void annotateTSC2AcceptorExon11Minus3Variant() {
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 16, 2_110_668, PositionType.ONE_BASED),
                "C", "G");

        final SimpleAnnotatable ann = new SimpleAnnotatable(variant, Transcripts.tsc2Transcripts(RD).get(0), Sequences.getTsc2Exon11Sequence(RD));

        final SimpleAnnotatable annotate = annotator.annotate(ann);
        annotate.getFeatureMap().entrySet()
                .stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.err.printf("%s=%s\n", e.getKey(), e.getValue()));
    }
}
