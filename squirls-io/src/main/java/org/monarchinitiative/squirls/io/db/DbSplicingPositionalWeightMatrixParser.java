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

import org.monarchinitiative.squirls.core.reference.DoubleMatrix;
import org.monarchinitiative.squirls.core.reference.SplicingParameters;
import org.monarchinitiative.squirls.core.reference.SplicingPwmData;
import org.monarchinitiative.squirls.io.CorruptedPwmException;
import org.monarchinitiative.squirls.io.SplicingPositionalWeightMatrixParser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This parser reads PWMs for splice donor, acceptor, and data for splicing parameters from database.
 */
public class DbSplicingPositionalWeightMatrixParser implements SplicingPositionalWeightMatrixParser {

    private final SplicingParameters splicingParameters;

    private final DoubleMatrix donorMatrix, acceptorMatrix;

    public DbSplicingPositionalWeightMatrixParser(DataSource dataSource) throws CorruptedPwmException {
        List<List<Double>> donorSite = parseMatrix(dataSource, "SPLICE_DONOR_SITE");
        this.donorMatrix = SplicingPositionalWeightMatrixParser.mapToDoubleMatrix(donorSite, EPSILON);

        List<List<Double>> acceptorSite = parseMatrix(dataSource, "SPLICE_ACCEPTOR_SITE");
        this.acceptorMatrix = SplicingPositionalWeightMatrixParser.mapToDoubleMatrix(acceptorSite, EPSILON);

        this.splicingParameters = parseSplicingParameters(dataSource);

    }

    private static List<List<Double>> parseMatrix(DataSource dataSource, String pwmName) throws CorruptedPwmException {
        // the outer list always have only 4 elements corresponding to A,C,G,T
        // the inner list represents columns - positions of the sequence
        List<List<Double>> data = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            data.add(new ArrayList<>());
        }

        // matrix
        String sql = "SELECT ROW_IDX, COL_IDX, CELL_VALUE FROM SQUIRLS.PWM_DATA WHERE PWM_NAME = ? ORDER BY ROW_IDX, COL_IDX ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pwmName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int row = rs.getInt("ROW_IDX");
                    double value = rs.getDouble("CELL_VALUE");
                    data.get(row).add(value);
                }
            }
        } catch (SQLException e) {
            throw new CorruptedPwmException(e);
        }

        return data;
    }

    private static SplicingParameters parseSplicingParameters(DataSource dataSource) throws CorruptedPwmException {
        int donorExonic = -1, donorIntronic = -1, acceptorExonic = -1, acceptorIntronic = -1;
        String sql = "SELECT PWM_NAME, PWM_KEY, PWM_VALUE FROM SQUIRLS.PWM_METADATA";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String name = rs.getString("PWM_NAME");
                String key = rs.getString("PWM_KEY");
                String value = rs.getString("PWM_VALUE");
                outer:
                switch (name) {
                    case "SPLICE_DONOR_SITE":
                        switch (key) {
                            case "EXON":
                                donorExonic = Integer.parseInt(value);
                                break outer;
                            case "INTRON":
                                donorIntronic = Integer.parseInt(value);
                                break outer;
                            default:
                                throw new CorruptedPwmException(String.format("Unknown key - %s", key));
                        }
                    case "SPLICE_ACCEPTOR_SITE":
                        switch (key) {
                            case "EXON":
                                acceptorExonic = Integer.parseInt(value);
                                break outer;
                            case "INTRON":
                                acceptorIntronic = Integer.parseInt(value);
                                break outer;
                            default:
                                throw new CorruptedPwmException(String.format("Unknown key - %s", key));
                        }
                    default:
                        throw new CorruptedPwmException(String.format("Unknown name - %s", name));
                }
            }
        } catch (SQLException e) {
            throw new CorruptedPwmException(e);
        }

        if (donorExonic < 0 || donorIntronic < 0 || acceptorExonic < 0 || acceptorIntronic < 0) {
            throw new CorruptedPwmException("Missing splice site definition data");
        }

        return SplicingParameters.of(donorExonic, donorIntronic, acceptorExonic, acceptorIntronic);
    }

    @Override
    public SplicingPwmData getSplicingPwmData() {
        return SplicingPwmData.builder()
                .setDonor(donorMatrix)
                .setAcceptor(acceptorMatrix)
                .setParameters(splicingParameters)
                .build();
    }
}
