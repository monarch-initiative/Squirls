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

import org.monarchinitiative.squirls.core.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.reference.TranscriptModelService;
import org.monarchinitiative.squirls.core.reference.TranscriptModelServiceOptions;
import org.monarchinitiative.squirls.io.SquirlsResourceException;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Daniel Danis
 */
public class TranscriptModelServiceDb implements TranscriptModelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptModelServiceDb.class);

    private final DataSource dataSource;

    private final GenomicAssembly genomicAssembly;

    private final Map<String, Integer> contigIdMap;

    private final int unknownContigId;

    private final boolean txSupportLevelIsAvailable;

    private final TranscriptModelServiceOptions options;

    private TranscriptModelServiceDb(DataSource dataSource, GenomicAssembly genomicAssembly, TranscriptModelServiceOptions options) throws SquirlsResourceException {
        this.dataSource = dataSource;
        this.genomicAssembly = genomicAssembly;
        sanityCheck();
        contigIdMap = new HashMap<>();
        for (Contig contig : genomicAssembly.contigs()) {
            if (contig.equals(Contig.unknown())) continue;

            contigIdMap.put(contig.name(), contig.id());
            contigIdMap.put(contig.genBankAccession(), contig.id());
            contigIdMap.put(contig.refSeqAccession(), contig.id());
            contigIdMap.put(contig.ucscName(), contig.id());
        }
        this.unknownContigId = Contig.unknown().id();

        this.options = options;

        this.txSupportLevelIsAvailable = checkIfTxSupportLevelIsAvailable();
        if (!txSupportLevelIsAvailable) {
            LOGGER.warn("Filtering transcripts by support level is disabled. Update SQUIRLS database");
        }
    }

    public static TranscriptModelServiceDb of(DataSource dataSource, GenomicAssembly genomicAssembly) throws SquirlsResourceException {
        return of(dataSource, genomicAssembly, TranscriptModelServiceOptions.defaultOptions());
    }

    public static TranscriptModelServiceDb of(DataSource dataSource, GenomicAssembly genomicAssembly, TranscriptModelServiceOptions options) throws SquirlsResourceException {
        return new TranscriptModelServiceDb(dataSource, genomicAssembly, options);
    }

    /**
     * This class requires the database to contain the tables <code>TRANSCRIPTS</code> and <code>SQUIRLS</code>
     */
    private void sanityCheck() throws SquirlsResourceException {
        Set<String> tableNames = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getTables(null, "SQUIRLS", null, new String[]{"TABLE"})) {
                while (rs.next())
                    tableNames.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new SquirlsResourceException(e);
        }

        Set<String> requiredTables = Set.of("TRANSCRIPTS", "EXONS");
        if (!tableNames.containsAll(requiredTables)) {
            throw new SquirlsResourceException("Missing at least one of the required tables `" + requiredTables + '`');
        }
    }

    private boolean checkIfTxSupportLevelIsAvailable() throws SquirlsResourceException {
        Set<String> transcriptsColumnNames = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            try (ResultSet rs = connection.getMetaData().getColumns(null, "SQUIRLS", "TRANSCRIPTS", null)) {
                while (rs.next())
                    transcriptsColumnNames.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            throw new SquirlsResourceException(e);
        }
        return transcriptsColumnNames.contains("TX_SUPPORT_LEVEL");
    }

    public int insertTranscript(TranscriptModel transcript) {
        int updated = 0;

        String txSql = "insert into SQUIRLS.TRANSCRIPTS(CONTIG, BEGIN, END, " +
                "BEGIN_ON_POS, END_ON_POS, STRAND, " +
                "TX_ACCESSION, HGVS_SYMBOL, CDS_START, CDS_END, TX_SUPPORT_LEVEL) " +
                "values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String exonSql = "insert into SQUIRLS.EXONS(TX_ID, BEGIN, END, EXON_NUMBER) VALUES ( ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement txPs = connection.prepareStatement(txSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement exonPs = connection.prepareStatement(exonSql)) {

                // insert transcript data
                txPs.setInt(1, contigIdMap.getOrDefault(transcript.contigName(), unknownContigId));
                txPs.setInt(2, transcript.startWithCoordinateSystem(CoordinateSystem.zeroBased()));
                txPs.setInt(3, transcript.endWithCoordinateSystem(CoordinateSystem.zeroBased()));
                txPs.setInt(4, transcript.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased()));
                txPs.setInt(5, transcript.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased()));
                txPs.setBoolean(6, transcript.strand().isPositive());
                txPs.setString(7, transcript.accessionId());
                txPs.setString(8, transcript.hgvsSymbol());
                if (transcript.isCoding()) {
                    GenomicRegion cds = transcript.cdsRegion().toZeroBased();
                    txPs.setInt(9, cds.startWithCoordinateSystem(CoordinateSystem.zeroBased()));
                    txPs.setInt(10, cds.endWithCoordinateSystem(CoordinateSystem.zeroBased()));
                } else {
                    txPs.setNull(9, Types.INTEGER);
                    txPs.setNull(10, Types.INTEGER);
                }
                txPs.setInt(11, transcript.transcriptSupportLevel());
                updated += txPs.executeUpdate();

                int txId;
                try (ResultSet rs = txPs.getGeneratedKeys()) {
                    txId = (rs.last()) ? rs.getInt(1) : 0;
                }

                // insert exons
                for (int i = 0; i < transcript.exons().size(); i++) {
                    GenomicRegion exon = transcript.exons().get(i);
                    exonPs.setInt(1, txId);
                    exonPs.setInt(2, exon.startWithCoordinateSystem(CoordinateSystem.zeroBased()));
                    exonPs.setInt(3, exon.endWithCoordinateSystem(CoordinateSystem.zeroBased()));
                    exonPs.setInt(4, i);
                    updated += exonPs.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Error occurred: {}", e.getMessage());
        }
        return updated;
    }

    @Override
    public List<String> getTranscriptAccessionIds() {
        String sql = "SELECT DISTINCT TX_ACCESSION FROM SQUIRLS.TRANSCRIPTS";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            List<String> accessions = new ArrayList<>();
            while (rs.next())
                accessions.add(rs.getString(1));
            return accessions;
        } catch (SQLException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Error occurred: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<TranscriptModel> overlappingTranscripts(GenomicRegion query) {
        String sql = getQueryForOverlappingTranscripts();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int contigId = contigIdMap.getOrDefault(query.contigName(), unknownContigId);
            statement.setInt(1, contigId);
            statement.setInt(2, query.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased()));
            statement.setInt(3, query.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased()));

            try (ResultSet rs = statement.executeQuery()) {
                return processTranscriptResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.warn("Error occurred: {}", e.getMessage());
            return List.of();
        }
    }

    private String getQueryForOverlappingTranscripts() {
        return txSupportLevelIsAvailable
                ?
                "select tx.TX_ID, tx.CONTIG, tx.STRAND, tx.BEGIN, tx.END, " +
                        "  tx.TX_ACCESSION, tx.HGVS_SYMBOL, tx.CDS_START, tx.CDS_END, tx.TX_SUPPORT_LEVEL, " +
                        "  e.EXON_NUMBER, e.BEGIN exon_begin, e.END exon_end " +
                        "from SQUIRLS.TRANSCRIPTS tx " +
                        " join SQUIRLS.EXONS e on tx.TX_ID = e.TX_ID " +
                        " where tx.CONTIG = ? " +
                        "   and ? < tx.END_ON_POS " +
                        "   and tx.BEGIN_ON_POS < ?"

                :
                "select tx.TX_ID, tx.CONTIG, tx.STRAND, tx.BEGIN, tx.END, " +
                        "  tx.TX_ACCESSION, tx.HGVS_SYMBOL, tx.CDS_START, tx.CDS_END, " +
                        "  e.EXON_NUMBER, e.BEGIN exon_begin, e.END exon_end " +
                        "from SQUIRLS.TRANSCRIPTS tx " +
                        " join SQUIRLS.EXONS e on tx.TX_ID = e.TX_ID " +
                        " where tx.CONTIG = ? " +
                        "   and ? < tx.END_ON_POS " +
                        "   and tx.BEGIN_ON_POS < ?";
    }

    @Override
    public Optional<TranscriptModel> transcriptByAccession(String txAccession) {
        String sql = getQueryForTranscriptByAccession();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txAccession);

            List<TranscriptModel> models;
            try (ResultSet rs = statement.executeQuery()) {
                models = processTranscriptResultSet(rs);
                if (models.size() == 1) {
                    return Optional.of(models.get(0));
                } else {
                    if (!models.isEmpty()) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Found {} distinct transcripts for the accession {}", models.size(), txAccession);
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Error occurred: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String getQueryForTranscriptByAccession() {
        return txSupportLevelIsAvailable
                ?
                "select tx.TX_ID, tx.CONTIG, tx.STRAND, tx.BEGIN, tx.END, " +
                        " tx.TX_ACCESSION, tx.HGVS_SYMBOL, tx.CDS_START, tx.CDS_END, tx.TX_SUPPORT_LEVEL, " +
                        " e.EXON_NUMBER, e.BEGIN exon_begin, e.END exon_end " +
                        " from SQUIRLS.TRANSCRIPTS tx " +
                        "  join SQUIRLS.EXONS e on tx.TX_ID = e.TX_ID " +
                        "  where tx.TX_ACCESSION = ?"

                :
                "select tx.TX_ID, tx.CONTIG, tx.STRAND, tx.BEGIN, tx.END, " +
                        " tx.TX_ACCESSION, tx.HGVS_SYMBOL, tx.CDS_START, tx.CDS_END, " +
                        " e.EXON_NUMBER, e.BEGIN exon_begin, e.END exon_end " +
                        " from SQUIRLS.TRANSCRIPTS tx " +
                        "  join SQUIRLS.EXONS e on tx.TX_ID = e.TX_ID " +
                        "  where tx.TX_ACCESSION = ?";
    }

    private List<TranscriptModel> processTranscriptResultSet(ResultSet rs) throws SQLException {
        Map<Integer, TranscriptModelBuilder> txMap = new HashMap<>();
        while (rs.next()) {
            if (txSupportLevelIsAvailable && !transcriptIsEligible(rs.getInt("TX_SUPPORT_LEVEL")))
                continue;
            rs.getInt("CDS_START");
            if (!options.useNoncodingTranscripts() && rs.wasNull())
                // rs is non-coding if CDS_START was null
                continue;

            int txId = rs.getInt(1);
            txMap.putIfAbsent(txId, TranscriptModelBuilder.builder());

            TranscriptModelBuilder builder = txMap.get(txId);
            int contig = rs.getInt("CONTIG");
            Strand strand = rs.getBoolean("STRAND") ? Strand.POSITIVE : Strand.NEGATIVE;

            builder.contig(genomicAssembly.contigById(contig));
            builder.strand(strand);
            builder.start(rs.getInt("BEGIN"));
            builder.end(rs.getInt("END"));
            builder.accessionId(rs.getString("TX_ACCESSION"));
            builder.hgvsSymbol(rs.getString("HGVS_SYMBOL"));
            builder.transcriptSupportLevel(rs.getInt("TX_SUPPORT_LEVEL"));
            int start = rs.getInt("CDS_START");
            builder.cdsStart(rs.wasNull() ? -1 : start);
            int end = rs.getInt("cds_end");
            builder.cdsEnd(rs.wasNull() ? -1 : end);
            builder.setExon(rs.getInt("EXON_NUMBER"), strand, rs.getInt("exon_begin"), rs.getInt("exon_end"));
        }

        return txMap.values().stream()
                .map(TranscriptModelBuilder::build)
                .collect(Collectors.toUnmodifiableList());
    }

    private boolean transcriptIsEligible(int transcriptSupportLevel) {
        // we use transcripts where TSL is missing (TSL = -1)
        return transcriptSupportLevel <= options.maxTxSupportLevel();
    }

}
