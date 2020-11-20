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

package org.monarchinitiative.squirls.ingest.dao;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.ingest.PojosForTesting;
import org.monarchinitiative.squirls.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_transcript_intron_exon_tables.sql"})
class TranscriptIngestDaoTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    private TranscriptIngestDao instance;

    private static void printAll(DataSource dataSource) throws Exception {
        String transcriptsSql = "SELECT * FROM SPLICING.TRANSCRIPTS";
        String exonsSql = "SELECT * FROM SPLICING.FEATURE_REGIONS";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement transcripts = connection.prepareStatement(transcriptsSql);
                 PreparedStatement exons = connection.prepareStatement(exonsSql)) {
                ResultSet trs = transcripts.executeQuery();
                System.out.println("=================   TRANSCRIPTS   =================");
                while (trs.next()) {
                    System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s",
                            trs.getString(1), trs.getString(2), trs.getString(3),
                            trs.getString(4), trs.getString(5), trs.getString(6),
                            trs.getString(7)));
                }

                System.out.println("=================      RECORDS      =================");
                ResultSet ers = exons.executeQuery();
                while (ers.next()) {
                    System.out.println(String.format("%s\t%s\t%s",
                            ers.getString(1), ers.getString(2), ers.getString(3)));
                }
            }
        }
    }

    private static RowMapper<String> rowMapper() {
        return (rs, i) -> rs.getString(1);
    }

    @BeforeEach
    void setUp() {
        instance = new TranscriptIngestDao(dataSource, referenceDictionary);
    }

    @Test
    void insertCorrectData() throws Exception {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<String> records = template.query("SELECT * FROM SPLICING.TRANSCRIPTS", rowMapper());
        assertThat(records, hasSize(0));

        SplicingTranscript alpha = PojosForTesting.makeAlphaTranscript(referenceDictionary);
        int inserted = instance.insertTranscript(alpha);
        SplicingTranscript beta = PojosForTesting.makeBetaTranscript(referenceDictionary);
        inserted += instance.insertTranscript(beta);

        records = template.query("SELECT * FROM SPLICING.TRANSCRIPTS", rowMapper());
        assertThat(records, hasSize(2));

        records = template.query("SELECT * FROM SPLICING.FEATURE_REGIONS", rowMapper());
        assertThat(records, hasSize(6));

        assertThat(inserted, is(2));
    }

    @Test
    void insertNull() {
        int i = instance.insertTranscript(null);
        assertThat(i, is(0));
    }

    @Test
    void insertEmptyData() {
        int i = instance.insertTranscript(SplicingTranscript.getDefaultInstance());
        assertThat(i, is(0));
    }
}