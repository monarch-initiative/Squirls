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

package org.monarchinitiative.squirls.autoconfigure;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.reference.StrandedSequenceService;
import org.monarchinitiative.squirls.core.reference.TranscriptModelService;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.squirls.initialize.SquirlsProperties;
import org.monarchinitiative.svart.GenomicAssembly;
import org.springframework.beans.factory.BeanCreationException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SquirlsAutoConfigurationTest extends AbstractAutoConfigurationTest {

    /**
     * Test how the normal configuration should look like and beans that should be available
     */
    @Test
    void testAllPropertiesSupplied() {
        load(SquirlsAutoConfiguration.class, "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710");
        /*
         * Data we expect to get from the user
         */
        Path threesDataDirectory = context.getBean("squirlsDataDirectory", Path.class);
        assertThat(threesDataDirectory.getFileName(), equalTo(Paths.get("data")));

        String squirlsGenomeAssembly = context.getBean("squirlsGenomeAssembly", String.class);
        assertThat(squirlsGenomeAssembly, is("hg19"));

        String squirlsDataVersion = context.getBean("squirlsDataVersion", String.class);
        assertThat(squirlsDataVersion, is("1710"));

        /*
         * Optional - default values
         */
        SquirlsProperties properties = context.getBean(SquirlsProperties.class);
        assertThat(properties.getClassifier().getVersion(), is("v0.4.6"));

        /*
         * High-level beans
         */
        assertThat(context.getBean("splicingAnnotator").getClass(), typeCompatibleWith(SplicingAnnotator.class));

        GenomicAssembly genomicAssembly = context.getBean("genomicAssembly", GenomicAssembly.class);
        assertThat(genomicAssembly, is(notNullValue()));
        assertThat(genomicAssembly.name(), equalTo("GRCh37.p13"));

        StrandedSequenceService strandedSequenceService = context.getBean("strandedSequenceService", StrandedSequenceService.class);
        assertThat(strandedSequenceService, is(notNullValue()));

        TranscriptModelService transcriptModelService = context.getBean("transcriptModelService", TranscriptModelService.class);
        assertThat(transcriptModelService, is(notNullValue()));

        SquirlsDataService squirlsDataService = context.getBean("squirlsDataService", SquirlsDataService.class);
        assertThat(squirlsDataService, is(notNullValue()));

        VariantSplicingEvaluator evaluator = context.getBean("variantSplicingEvaluator", VariantSplicingEvaluator.class);
        assertThat(evaluator, is(notNullValue()));
    }

    @Test
    void testOptionalProperties() {
        load(SquirlsAutoConfiguration.class, "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.classifier.version=v0.4.4",
                "squirls.classifier.max-variant-length=50",
                "squirls.annotator.version=agez"
        );

        SquirlsProperties properties = context.getBean(SquirlsProperties.class);
        assertThat(properties.getClassifier().getVersion(), is("v0.4.4"));
        assertThat(properties.getClassifier().getMaxVariantLength(), is(50));
        assertThat(properties.getAnnotator().getVersion(), is("agez"));
    }

    @Test
    void testMissingDataDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Path to Squirls data directory (`--squirls.data-directory`) is not specified"));
    }

    @Test
    void testProvidedPathDoesNotPointToDirectory() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA + "/rocket",
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Path to Squirls data directory 'src/test/resources/data/rocket' does not point to real directory"));

    }

    @Test
    void testMissingGenomeAssembly() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
//                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710"));
        assertThat(thrown.getMessage(), containsString("Genome assembly (`--squirls.genome-assembly`) is not specified"));
    }

    @Test
    void testMissingDataVersion() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19"
//                "squirls.data-version=1710",
        ));
        assertThat(thrown.getMessage(), containsString("Data version (`--squirls.data-version`) is not specified"));
    }

    @Test
    void testNonExistingClassifier() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.classifier.version=v0.0.0"));
        assertThat(thrown.getMessage(), containsString("Classifier with version v0.0.0 has never been used in Squirls"));
    }

    @Test
    void testNonPresentClassifier() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.classifier.version=v0.4.1"));
        assertThat(thrown.getMessage(), containsString("Classifier version `v0_4_1` is not available, choose one from {v0_4_4, v0_4_6}"));
    }

    @Test
    void testNonExistingSplicingAnnotator() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(SquirlsAutoConfiguration.class,
                "squirls.data-directory=" + TEST_DATA,
                "squirls.genome-assembly=hg19",
                "squirls.data-version=1710",
                "squirls.annotator.version=non-existing"));
        assertThat(thrown.getMessage(), containsString("invalid 'squirls.annotator.version' property value: `non-existing`"));
    }
}