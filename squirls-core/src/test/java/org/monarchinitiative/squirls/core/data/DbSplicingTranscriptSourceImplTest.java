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

package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_refdict_tables.sql", "insert_refdict_data.sql",
        "create_transcripts_tables.sql", "insert_transcripts_data.sql"})
class DbSplicingTranscriptSourceImplTest {


    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    private DbSplicingTranscriptSource source;

    @BeforeEach
    void setUp() {
        source = new DbSplicingTranscriptSource(dataSource);
    }

    @Test
    void transcriptsExonsAndIntronsAreDecoded() {
        // we expect to get transcripts `FIRST` and `THIRD` by using these coordinates
        List<SplicingTranscript> transcripts = source.fetchTranscripts("chr1", 1000, 5000, referenceDictionary);
        assertThat(transcripts, hasSize(2));

        // 2 transcripts
        transcripts.sort(Comparator.comparing(SplicingTranscript::getAccessionId));

        // ---     FIRST      ---
        SplicingTranscript first = transcripts.get(0);
        assertThat(first.getAccessionId(), is("FIRST"));
        assertThat(first.getChrName(), is("chr1"));
        assertThat(first.getTxBegin(), is(1000));
        assertThat(first.getTxEnd(), is(2000));
        assertThat(first.getStrand(), is(Strand.FWD));

        assertThat(first.getExons(), is(List.of(
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 1200)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1400, 1600)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1800, 2000)).build()
        )));

        assertThat(first.getIntrons(), is(List.of(
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1200, 1400)).setDonorScore(9.433).setAcceptorScore(7.392).build(),
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1600, 1800)).setDonorScore(4.931).setAcceptorScore(7.832).build())
        ));

        // ---     THIRD     ---
        SplicingTranscript third = transcripts.get(1);
        assertThat(third.getAccessionId(), is("THIRD"));
        assertThat(third.getChrName(), is("chr1"));
        assertThat(third.getTxBegin(), is(8_000));
        assertThat(third.getTxEnd(), is(10_000));
        assertThat(third.getStrand(), is(Strand.REV));

        assertThat(third.getExons(), is(List.of(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8000, 8200)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8300, 8500)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8900, 9600)).build(),
                SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 9800, 10000)).build())));

        assertThat(third.getIntrons(), is(List.of(
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8200, 8300)).setDonorScore(8.429).setAcceptorScore(4.541).build(),
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 8500, 8900)).setDonorScore(5.249).setAcceptorScore(2.946).build(),
                SplicingIntron.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.REV, 1, 9600, 9800)).setDonorScore(4.234).setAcceptorScore(1.493).build())));
    }

    @Test
    void fetchTranscriptByAccession() {
        final Optional<SplicingTranscript> txOpt = source.fetchTranscriptByAccession("SECOND", referenceDictionary);
        assertThat(txOpt.isPresent(), is(true));

        final SplicingTranscript tx = txOpt.get();
        assertThat(tx, is(SplicingTranscript.builder()
                .setAccessionId("SECOND")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5000, 6000))
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5000, 5100)).build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5100, 5300))
                        .setDonorScore(5.329)
                        .setAcceptorScore(3.848)
                        .build())
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5300, 5500)).build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5500, 5800))
                        .setDonorScore(9.740)
                        .setAcceptorScore(6.348)
                        .build())
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5800, 5900)).build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5900, 5950))
                        .setDonorScore(5.294)
                        .setAcceptorScore(8.239)
                        .build())
                .addExon(SplicingExon.builder().setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 5950, 6000)).build())
                .build()));
    }

    @Test
    void fetchNonExistingTranscript() {
        final Optional<SplicingTranscript> txOpt = source.fetchTranscriptByAccession("BLABLA", referenceDictionary);
        assertThat(txOpt.isEmpty(), is(true));
    }

    @Test
    void getTranscriptAccessionIds() {
        final List<String> ids = source.getTranscriptAccessionIds();
        assertThat(ids, hasSize(3));
        assertThat(ids, hasItems("FIRST", "SECOND", "THIRD"));
    }
}