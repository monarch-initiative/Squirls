package org.monarchinitiative.threes.core.scoring;

class SMSScorerTest extends ScorerTestBase {

    /*

    SCORE IS NOT BEING USED AT THE MOMENT
    private static final double EPSILON = 0.0005;

    private SMSScorer scorer;

    @Autowired
    private SMSCalculator calculator;

    @BeforeEach
    void setUp() {
        scorer = new SMSScorer(calculator);
    }


    @Test
    void scoreSnp() {
        // arrange
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1100)
                        .setEnd(1101)
                        .setStrand(true)
                        .build())
                .setRef("T")
                .setAlt("C")
                .build();

        SplicingTernate ternate = SplicingTernate.of(variant, st.getExons().get(0), sequenceInterval);

        // act
        double score = scorer.scoringFunction().apply(ternate);

        // assert
        assertThat(score, is(closeTo(-1.0703D, EPSILON)));
    }


    @Test
    void scoreInsertion() {
        // arrange
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1100)
                        .setEnd(1101)
                        .setStrand(true)
                        .build())
                .setRef("T")
                .setAlt("TGGG")
                .build();

        SplicingTernate ternate = SplicingTernate.of(variant, st.getExons().get(0), sequenceInterval);

        // act
        double score = scorer.scoringFunction().apply(ternate);

        // assert
        assertThat(score, is(closeTo(0.7605D, EPSILON)));
    }


    @Test
    void scoreDeletion() {
        // arrange
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1100)
                        .setEnd(1104)
                        .setStrand(true)
                        .build())
                .setRef("TTCA")
                .setAlt("T")
                .build();

        SplicingTernate ternate = SplicingTernate.of(variant, st.getExons().get(0), sequenceInterval);

        // act
        double score = scorer.scoringFunction().apply(ternate);

        // assert
        assertThat(score, is(closeTo(-0.0587D, EPSILON)));
    }
     */
}