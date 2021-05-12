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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.SquirlsFeatures;
import org.monarchinitiative.squirls.core.reference.StrandedSequence;
import org.monarchinitiative.squirls.core.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.svart.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Here we test a real-world variant as an integration test.
 */
@SpringBootTest(classes = TestDataSourceConfig.class)
public class VariantSplicingEvaluatorDefaultTest {

    private static StrandedSequence sequence;

    private static GenomicAssembly assembly;

    @Mock
    private SquirlsDataService squirlsDataService;

    @Mock
    private SplicingAnnotator annotator;

    @Mock
    private SquirlsClassifier classifier;

    private VariantSplicingEvaluatorDefault evaluator;


    @BeforeAll
    public static void beforeAll() {
        Contig chr9 = Contig.of(1, "9", SequenceRole.ASSEMBLED_MOLECULE, "9", AssignedMoleculeType.CHROMOSOME, 141_213_431,
                "CM000671.1", "NC_000009.11", "chr9");
        assembly = GenomicAssembly.of("GRCh37.custom", "Homo sapiens (human)", "9606",
                "Me", "2020-01-04", "GB1", "RS1",
                List.of(chr9));
        char[] chars = new char[136_230_000 - 136_210_000 + 1];
        Arrays.fill(chars, 'A'); // the sequence does not really matter since we use mocks
        sequence = StrandedSequence.of(chr9, Strand.POSITIVE, CoordinateSystem.oneBased(),
                Position.of(136_210_000), Position.of(136_230_000), new String(chars));
    }

    @BeforeEach
    public void setUp() {
        when(squirlsDataService.genomicAssembly()).thenReturn(assembly);
        evaluator = VariantSplicingEvaluatorDefault.of(squirlsDataService, annotator, classifier);
    }

    @Test
    public void evaluateWrtTx() {
        Contig chr9 = assembly.contigByName("9");
        Variant variant = Variant.of(chr9, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(136_223_949), "G", "C");

        // 0 - squirls data service
        TranscriptModel stx = PojosForTesting.surf2_NM_017503_5(chr9);
        when(squirlsDataService.transcriptByAccession("NM_017503.5")).thenReturn(Optional.of(stx));
        when(squirlsDataService.sequenceForRegion(any(GenomicRegion.class))).thenReturn(sequence);

        // 1 - splicing annotator
        VariantOnTranscript vot = VariantOnTranscript.of(variant, stx, sequence);
        SquirlsFeatures features = SquirlsFeatures.of(Map.of("donor_offset", 5., "acceptor_offset", 1234.)); // not real feature values
        when(annotator.annotate(vot)).thenReturn(features);

        // 2 - classifier
        Prediction prediction = Prediction.of(
                PartialPrediction.of("donor", .6, .7),
                PartialPrediction.of("acceptor", .1, .6));
        when(classifier.predict(features)).thenReturn(prediction);


        // -------------------------------------------------------------------------------------------------------------
        SquirlsResult squirlsResult = evaluator.evaluate(variant, Set.of("NM_017503.5"));
        // -------------------------------------------------------------------------------------------------------------


        assertThat(squirlsResult.txAccessionIds().size(), is(1));
        assertThat(squirlsResult.txAccessionIds(), hasItem("NM_017503.5"));


        Optional<SquirlsTxResult> predOpt = squirlsResult.resultForTranscript("NM_017503.5");
        assertThat(predOpt.isPresent(), is(true));
        SquirlsTxResult actual = predOpt.get();

        assertThat(actual.featureValue("donor_offset").orElseThrow(), is(5.));
        assertThat(actual.featureValue("acceptor_offset").orElseThrow(), is(1234.));
        assertThat(actual.prediction(), is(prediction));

        verify(squirlsDataService).genomicAssembly();
        verify(squirlsDataService).sequenceForRegion(GenomicRegion.of(chr9, Strand.POSITIVE, CoordinateSystem.zeroBased(), 136_223_275, 136_228_184));
        verify(squirlsDataService).transcriptByAccession("NM_017503.5");
        verify(annotator).annotate(vot);
        verify(classifier).predict(features);
    }

    @Test
    public void evaluateWrtTx_unknownContig() {
        Contig unknown = Contig.of(1_000, "Unknown", SequenceRole.ASSEMBLED_MOLECULE, "Unknown", AssignedMoleculeType.CHROMOSOME, 1_000_000_000, "", "", "");
        Variant variant = Variant.of(unknown,"", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(136_223_949), "G", "C");
        SquirlsResult squirlsResult = evaluator.evaluate(variant);

        assertThat(squirlsResult.isEmpty(), is(true));
    }

    @Test
    public void evaluateWrtTx_unknownTx() {
        when(squirlsDataService.transcriptByAccession("BLABLA")).thenReturn(Optional.empty());
        Contig chr9 = assembly.contigByName("9");

        Variant variant = Variant.of(chr9, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(136_223_949), "G", "C");

        // -------------------------------------------------------------------------------------------------------------
        SquirlsResult squirlsResult = evaluator.evaluate(variant, Set.of("BLABLA"));
        // -------------------------------------------------------------------------------------------------------------

        assertThat(squirlsResult.isEmpty(), is(true));
    }

    @Test
    public void evaluateWrtTx_notEnoughSequenceAvailable() {
        Contig chr9 = assembly.contigByName("9");
        TranscriptModel stx = PojosForTesting.surf2_NM_017503_5(chr9);
        when(squirlsDataService.transcriptByAccession("NM_017503.5")).thenReturn(Optional.of(stx));
        when(squirlsDataService.sequenceForRegion(any(GenomicRegion.class))).thenReturn(null);

        Variant variant = Variant.of(chr9, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(136_223_949), "G", "C");

        // -------------------------------------------------------------------------------------------------------------
        SquirlsResult squirlsResult = evaluator.evaluate(variant, Set.of("NM_017503.5"));
        // -------------------------------------------------------------------------------------------------------------

        assertThat(squirlsResult.isEmpty(), is(true));
    }

    /**
     * This test only specifies variant coordinates, thus it is evaluated with respect to all transcripts it overlaps
     * with. In this case, we evaluate the variant wrt one transcript <code>stx</code>.
     */
    @Test
    public void evaluateWrtCoordinates() {
        Contig chr9 = assembly.contigByName("9");
        Variant variant = Variant.of(chr9, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(136_223_949), "G", "C");

        TranscriptModel stx = PojosForTesting.surf2_NM_017503_5(chr9);

        when(squirlsDataService.sequenceForRegion(any(GenomicRegion.class))).thenReturn(sequence);
        when(squirlsDataService.overlappingTranscripts(variant.toZeroBased())).thenReturn(List.of(stx));

        when(squirlsDataService.sequenceForRegion(any(GenomicRegion.class))).thenReturn(sequence);

        VariantOnTranscript vot = VariantOnTranscript.of(variant, stx, sequence);
        SquirlsFeatures features = SquirlsFeatures.of(Map.of("donor_offset", 5., "acceptor_offset", 1234.)); // not real

        when(annotator.annotate(vot)).thenReturn(features);

        Prediction prediction = Prediction.of(
                PartialPrediction.of("donor", .6, .7),
                PartialPrediction.of("acceptor", .1, .6));

        when(classifier.predict(features)).thenReturn(prediction);


        // -------------------------------------------------------------------------------------------------------------
        SquirlsResult squirlsResult = evaluator.evaluate(variant);
        // -------------------------------------------------------------------------------------------------------------


        assertThat(squirlsResult.txAccessionIds(), hasSize(1));
        assertThat(squirlsResult.txAccessionIds(), hasItem("NM_017503.5"));

        Optional<SquirlsTxResult> resOpt = squirlsResult.resultForTranscript("NM_017503.5");
        assertThat(resOpt.isPresent(), is(true));

        SquirlsTxResult actual = resOpt.get();

        assertThat(actual.featureValue("donor_offset").orElseThrow(), is(5.));
        assertThat(actual.featureValue("acceptor_offset").orElseThrow(), is(1234.));
        assertThat(actual.prediction(), is(prediction));

        verify(squirlsDataService).genomicAssembly();
        verify(squirlsDataService).sequenceForRegion(GenomicRegion.of(chr9, Strand.POSITIVE, CoordinateSystem.zeroBased(), 136_223_275, 136_228_184));
        verify(squirlsDataService).overlappingTranscripts(variant.toZeroBased());
        verify(annotator).annotate(vot);
        verify(classifier).predict(features);
    }
}