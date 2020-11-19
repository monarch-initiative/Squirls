/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

package org.monarchinitiative.squirls.core;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.classifier.PartialPrediction;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.IdentityTransformer;
import org.monarchinitiative.squirls.core.data.SplicingTranscriptSource;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Here we test some real-world variants.
 */
@SpringBootTest(classes = TestDataSourceConfig.class)
class VariantSplicingEvaluatorDefaultTest {

    /**
     * Tolerance for numeric comparisons.
     */
    private static final double EPSILON = 5E-6;

    private static SequenceInterval SI;

    private static ReferenceDictionary RD;

    @Mock
    private GenomeSequenceAccessor accessor;

    @Mock
    private SplicingTranscriptSource transcriptSource;

    @Mock
    private SplicingAnnotator annotator;

    @Mock
    private SquirlsClassifier classifier;

    private VariantSplicingEvaluatorDefault evaluator;


    @BeforeAll
    static void beforeAll() {
        final ReferenceDictionaryBuilder rdBuilder = new ReferenceDictionaryBuilder();
        rdBuilder.putContigID("chr9", 9);
        rdBuilder.putContigID("9", 9);
        rdBuilder.putContigName(9, "chr9");
        rdBuilder.putContigLength(9, 141_213_431);
        RD = rdBuilder.build();
        char[] chars = new char[136_230_000 - 136_210_000 + 1];
        Arrays.fill(chars, 'A'); // the sequence does not really matter since we use mocks
        SI = SequenceInterval.of(new GenomeInterval(RD, Strand.FWD, 9, 136_210_000, 136_230_000, PositionType.ONE_BASED), new String(chars));
    }

    @BeforeEach
    void setUp() {
        // genome sequence accessor
        when(accessor.getReferenceDictionary()).thenReturn(RD);
        evaluator = VariantSplicingEvaluatorDefault.builder()
                .accessor(accessor)
                .txSource(transcriptSource)
                .annotator(annotator)
                .classifier(classifier)
                .transformer(IdentityTransformer.getInstance())
                .build();
    }

    @Test
    void evaluateWrtTx() throws Exception {
        // arrange
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C");

        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscriptByAccession("NM_017503.5", RD))
                .thenReturn(Optional.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.of(SI));

        // 2 - splicing annotator
        SplicingPredictionData plain = SplicingPredictionDataDefault.of(variant, stx, SI);
        SplicingPredictionData annotated = SplicingPredictionDataDefault.of(variant, stx, SI);
        annotated.putFeature("donor_offset", 5);
        annotated.putFeature("acceptor_offset", 1234); // not real
        when(annotator.annotate(plain)).thenReturn(annotated);

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.of(
                PartialPrediction.of("donor", .6, .7),
                PartialPrediction.of("acceptor", .1, .6));
        SplicingPredictionData predicted = SplicingPredictionDataDefault.of(variant, stx, SI);
        predicted.putFeature("donor_offset", 5);
        predicted.putFeature("acceptor_offset", 1234); // not real
        predicted.setPrediction(prediction);
        when(classifier.predict(annotated)).thenReturn(predicted);

        // act
        SquirlsResult squirlsResult = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));


        // assert
        assertThat(squirlsResult.txAccessionIds().size(), is(1));
        assertThat(squirlsResult.txAccessionIds(), hasItem("NM_017503.5"));


        Optional<SquirlsTxResult> predOpt = squirlsResult.resultForTranscript("NM_017503.5");
        assertThat(predOpt.isPresent(), is(true));
        SquirlsTxResult actual = predOpt.get();

        assertThat(actual.featureValue("donor_offset"), is(5.));
        assertThat(actual.featureValue("acceptor_offset"), is(1234.));
        assertThat(actual.prediction(), is(prediction));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_176, 136_228_284, PositionType.ONE_BASED));
        verify(annotator).annotate(plain);
    }

    @Test
    void evaluateWrtTx_unknownContig() {
        // arrange & act
        SquirlsResult squirlsResult = evaluator.evaluate("BLA", 100, "G", "C");

        // assert
        assertThat(squirlsResult.isEmpty(), is(true));
    }

    @Test
    void evaluateWrtTx_unknownTx() {
        // arrange
        when(transcriptSource.fetchTranscriptByAccession("BLABLA", RD)).thenReturn(Optional.empty());

        // act
        SquirlsResult squirlsResult = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("BLABLA"));

        // assert
        assertThat(squirlsResult.isEmpty(), is(true));
    }

    @Test
    void evaluateWrtTx_notEnoughSequenceAvailable() {
        // arrange
        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscriptByAccession("NM_017503.5", RD)).thenReturn(Optional.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.empty());

        // act
        SquirlsResult squirlsResult = evaluator.evaluate("chr9", 136_223_949, "G", "C", Set.of("NM_017503.5"));

        // assert
        assertThat(squirlsResult.isEmpty(), is(true));
    }

    /**
     * This test only specifies variant coordinates, thus it is evaluated with respect to all transcripts it overlaps
     * with. In this case, we evaluate the variant wrt one transcript <code>stx</code>.
     */
    @Test
    void evaluateWrtCoordinates() throws Exception {
        // arrange
        final GenomeVariant variant = new GenomeVariant(new GenomePosition(RD, Strand.FWD, 9, 136_223_949, PositionType.ONE_BASED), "G", "C");

        // 0 - splicing transcript source
        final SplicingTranscript stx = PojosForTesting.surf2_NM_017503_5(RD);
        when(transcriptSource.fetchTranscripts("chr9", 136_223_948, 136_223_949, RD)).thenReturn(List.of(stx));

        // 1 - genome sequence accessor
        when(accessor.fetchSequence(any(GenomeInterval.class))).thenReturn(Optional.of(SI));

        // 2 - splicing annotator
        final SplicingPredictionData plain = SplicingPredictionDataDefault.of(variant, stx, SI);
        final SplicingPredictionData annotated = SplicingPredictionDataDefault.of(variant, stx, SI);
        annotated.putFeature("donor_offset", 5);
        annotated.putFeature("acceptor_offset", 1234); // not real

        when(annotator.annotate(plain)).thenReturn(annotated);

        // 3 - classifier
        StandardPrediction prediction = StandardPrediction.of(
                PartialPrediction.of("donor", .6, .7),
                PartialPrediction.of("acceptor", .1, .6));
        SplicingPredictionData predicted = SplicingPredictionDataDefault.of(variant, stx, SI);
        predicted.putFeature("donor_offset", 5);
        predicted.putFeature("acceptor_offset", 1234); // not real
        predicted.setPrediction(prediction);

        when(classifier.predict(annotated)).thenReturn(predicted);

        // act
        SquirlsResult squirlsResult = evaluator.evaluate("chr9", 136_223_949, "G", "C");

        // assert
        assertThat(squirlsResult.txAccessionIds(), hasSize(1));
        assertThat(squirlsResult.txAccessionIds(), hasItem("NM_017503.5"));

        Optional<SquirlsTxResult> resOpt = squirlsResult.resultForTranscript("NM_017503.5");
        assertThat(resOpt.isPresent(), is(true));

        SquirlsTxResult actual = resOpt.get();

        assertThat(actual.featureValue("donor_offset"), is(5.));
        assertThat(actual.featureValue("acceptor_offset"), is(1234.));
        assertThat(actual.prediction(), is(prediction));

        verify(accessor).fetchSequence(new GenomeInterval(RD, Strand.FWD, 9, 136_223_176, 136_228_284, PositionType.ONE_BASED));
        verify(annotator).annotate(plain);
    }
}