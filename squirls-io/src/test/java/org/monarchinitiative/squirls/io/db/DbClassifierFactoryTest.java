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

package org.monarchinitiative.squirls.io.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;
import org.monarchinitiative.squirls.io.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = {TestDataSourceConfig.class})
public class DbClassifierFactoryTest {

    private static final double TOLERANCE = 5E-12;

    @Autowired
    public DataSource dataSource;

    private DbClassifierFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new DbClassifierFactory(dataSource);
    }

    @Test
    @Sql(scripts = {"create_classifier_table.sql"})
    public void storeClassifier() throws Exception {
        byte[] payload = new byte[]{-128, 6, 0, 88, 127};
        SquirlsClassifierVersion version = SquirlsClassifierVersion.v0_4_1;
        int updated = factory.storeClassifier(version, payload);

        byte[] actual = null;
        SquirlsClassifierVersion actualVersion = null;
        int i = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("select version, data from SQUIRLS.CLASSIFIER where version = ?")) {
            ps.setString(1, version.toString());
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                if (i == 0) {
                    actualVersion = SquirlsClassifierVersion.valueOf(rs.getString("version"));
                    actual = rs.getBytes("data");
                }
                i++;
            }
        }

        assertThat(i, is(1));
        assertThat(updated, is(1));
        assertThat(actualVersion, is(SquirlsClassifierVersion.v0_4_1));
        assertThat(payload, is(actual));
    }

    @Test
    @Sql(scripts = "create_classifier_table.sql",
            statements = "insert into SQUIRLS.CLASSIFIER(version, data) values ('v0_4_1', '000F10FF')")
    public void readClassifier() throws Exception {
        byte[] bytes = factory.readClassifierBytes(SquirlsClassifierVersion.v0_4_1);
        assertThat(bytes, equalTo(new byte[]{0, 15, 16, -1}));

        byte[] na = factory.readClassifierBytes(SquirlsClassifierVersion.v0_4_6);
        assertThat(na, equalTo(null));
    }

    @Test
    @Sql(scripts = "create_classifier_table.sql",
            statements = "insert into SQUIRLS.CLASSIFIER(version, data) " +
                    " values ('v0_4_1', 'BEEFBEEF'), ('v0_4_6', 'BEEFBEEFBEEFBEEF')")
    public void getAllClassifiers() {
        Collection<SquirlsClassifierVersion> clfs = factory.getAvailableClassifiers();
        assertThat(clfs, hasSize(2));
        assertThat(clfs, hasItems(SquirlsClassifierVersion.v0_4_1, SquirlsClassifierVersion.v0_4_6));
    }

    @Test
    public void jsonify() {
        Map<String, Double> parameters = Map.of("bla", 0.123456789012, "kva", 11.998877665544);
        String payload = DbClassifierFactory.jsonify(parameters);
        assertThat(payload, is("{\"bla\": 0.123456789012, \"kva\": 11.998877665544}"));
    }

    @Test
    public void deJsonify() {
        String payload = "{\"bla\": 0.123456789012, \"kva\": 11.998877665544}";
        Map<String, Double> params = DbClassifierFactory.deJsonify(payload);

        assertThat(params.size(), is(2));
        assertThat(params.keySet(), hasItems("bla", "kva"));
        assertThat(params.get("bla"), is(closeTo(0.123456789012, TOLERANCE)));
        assertThat(params.get("kva"), is(closeTo(11.998877665544, TOLERANCE)));
    }
}