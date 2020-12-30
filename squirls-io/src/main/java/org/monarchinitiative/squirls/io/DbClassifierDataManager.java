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

package org.monarchinitiative.squirls.io;

import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This dao handles business regarding classifier and prediction process.
 */
public class DbClassifierDataManager implements ClassifierDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbClassifierDataManager.class);

    private static final Pattern DEJSONIFY_PATTERN = Pattern.compile("\"(?<key>\\w+)\":\\s*(?<value>-?\\d+\\.\\d*)");

    private final DataSource dataSource;

    public DbClassifierDataManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Turn parameter map into a JSON-like string.
     *
     * @param parameters map with parameters
     * @return JSON-like string
     */
    static String jsonify(Map<String, Double> parameters) {
        return parameters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("\"%s\": %.12f", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Turn a JSON string containing <em>String -> Double</em> mappings to a corresponding Map.
     *
     * @param payload to de-jsonify
     * @return Map with decoded values
     */
    static Map<String, Double> deJsonify(String payload) {
        Map<String, Double> map = new HashMap<>();
        final Matcher matcher = DEJSONIFY_PATTERN.matcher(payload);
        while (matcher.find()) {
            map.put(matcher.group("key"), Double.parseDouble(matcher.group("value")));
        }

        return map;
    }

    /**
     * @return collection versions of available classifiers
     */
    @Override
    public Set<SquirlsClassifierVersion> getAvailableClassifiers() {
        String sql = "select version from SPLICING.CLASSIFIER";
        final Set<SquirlsClassifierVersion> versions = new HashSet<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    versions.add(SquirlsClassifierVersion.valueOf(rs.getString("version")));
            }

        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return versions;
    }

    @Override
    public int storeClassifier(SquirlsClassifierVersion version, byte[] clfBytes) {
        int updated = 0;
        String sql = "insert into SPLICING.CLASSIFIER(version, data) values (?, ?)";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, version.toString());
            ps.setBytes(2, clfBytes);
            updated += ps.executeUpdate();
            return updated;
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
            return 0;
        }
    }

    byte[] readClassifierBytes(SquirlsClassifierVersion version) throws SQLException {
        String sql = "select data from SPLICING.CLASSIFIER where version = ?";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, version.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.first()) {
                    return rs.getBytes("data");
                }
            }
        }
        return null;
    }

    /**
     * Returns classifier optional corresponding to a given {@code version}.
     *
     * @param version version string, e.g. `v1.0.0`
     * @return classifier optional
     */
    @Override
    public Optional<SquirlsClassifier> readClassifier(SquirlsClassifierVersion version) {
        SquirlsClassifierDeserializer deserializer = SquirlsClassifierDeserializer.forVersion(version);
        try {
            return Optional.ofNullable(deserializer.deserialize(new ByteArrayInputStream(readClassifierBytes(version))));
        } catch (SQLException sql) {
            LOGGER.warn("Error reading classifier {} from the database: {}:{}", version, sql.getErrorCode(), sql.getMessage());
        } catch (SquirlsSerializationException e) {
            LOGGER.warn("Error when deserializing classifier {}: {}", version, e.getMessage());
        }
        return Optional.empty();
    }
}
