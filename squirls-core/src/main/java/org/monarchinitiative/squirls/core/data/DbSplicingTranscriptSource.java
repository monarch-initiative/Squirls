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
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DbSplicingTranscriptSource extends BaseDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSplicingTranscriptSource.class);


    public DbSplicingTranscriptSource(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<String> getTranscriptAccessionIds() {
        List<String> accessions = new ArrayList<>();
        String sql = "select TX_ACCESSION from SPLICING.TRANSCRIPTS";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accessions.add(rs.getString("TX_ACCESSION"));
            }
        } catch (SQLException e) {
            LOGGER.warn("Error during getting transcript accessions: ", e);
            return Collections.emptyList();
        }
        return accessions;
    }

    @Override
    public List<SplicingTranscript> fetchTranscripts(String contig, int begin, int end, ReferenceDictionary referenceDictionary) {
        if (!internalReferenceDictionary.getContigNameToID().containsKey(contig)) {
            LOGGER.warn("Contig `{}` is not represented in database", contig);
            return Collections.emptyList();
        }
        final int dbChr = internalReferenceDictionary.getContigNameToID().get(contig);

        if (!referenceDictionary.getContigNameToID().containsKey(contig)) {
            LOGGER.warn("Query asks for contig `{}` which is not represented in provided reference dictionary", contig);
            return Collections.emptyList();
        }
        final int queryChr = referenceDictionary.getContigNameToID().get(contig);

        String sql = "select tx.CONTIG, " +
                " tx.BEGIN_POS as tx_begin, " +
                " tx.END_POS as tx_end, " +
                " tx.STRAND, " +
                " tx.TX_ACCESSION, " +
                " fr.BEGIN_POS as fr_begin, " +
                " fr.END_POS as fr_end, " +
                " fr.REGION_TYPE, " +
                " fr.REGION_NUMBER, " +
                " fr.PROPERTIES " +
                " from SPLICING.TRANSCRIPTS tx " +
                "   left join SPLICING.FEATURE_REGIONS fr on tx.TX_ACCESSION = fr.TX_ACCESSION " +
                " where tx.CONTIG = ? " +
                "   and ? < tx.END_ON_FWD " +
                "   and tx.BEGIN_ON_FWD < ?;";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dbChr);
            statement.setInt(2, begin);
            statement.setInt(3, end);

            try (ResultSet rs = statement.executeQuery()) {
                Map<String, SplicingTranscript.Builder> txBuilderMap = new HashMap<>();
                Map<String, Map<Integer, SplicingExon>> exons = new HashMap<>();
                Map<String, Map<Integer, SplicingIntron>> introns = new HashMap<>();
                while (rs.next()) {
                    final String txAccession = rs.getString("TX_ACCESSION").strip();
                    final int txBegin = rs.getInt("tx_begin");
                    final int txEnd = rs.getInt("tx_end");
                    final Strand strand = rs.getBoolean("STRAND") ? Strand.FWD : Strand.REV;
                    if (!txBuilderMap.containsKey(txAccession)) {
                        /*
                        this is the first time we see data regarding the transcript with `txAccession`,
                        we need to add transcript-related data, such as accession and interval
                         */
                        final GenomeInterval txInterval = new GenomeInterval(referenceDictionary, strand, queryChr, txBegin, txEnd);
                        txBuilderMap.put(txAccession, SplicingTranscript.builder()
                                .setAccessionId(txAccession)
                                .setCoordinates(txInterval));
                        exons.put(txAccession, new HashMap<>());
                        introns.put(txAccession, new HashMap<>());
                    }
                    final GenomeInterval featureInterval = new GenomeInterval(referenceDictionary, strand, queryChr, rs.getInt("fr_begin"), rs.getInt("fr_end"));

                    final String regType = rs.getString("REGION_TYPE");
                    final int regionNumber = rs.getInt("REGION_NUMBER");

                    if (regType.equals(SplicingTranscript.EXON_REGION_CODE)) {
                        // this SQL record represents exon
                        exons.get(txAccession).put(regionNumber,
                                SplicingExon.builder()
                                        .setInterval(featureInterval)
                                        .build());
                    } else if (regType.equals(SplicingTranscript.INTRON_REGION_CODE)) {
                        // this SQL record represents intron
                        try {
                            final String properties = rs.getString("PROPERTIES");
                            final IntronProperties intronProperties = IntronProperties.parseString(properties);

                            introns.get(txAccession).put(regionNumber,
                                    SplicingIntron.builder()
                                            .setInterval(featureInterval)
                                            .setDonorScore(intronProperties.getDonorScore())
                                            .setAcceptorScore(intronProperties.getAcceptorScore())
                                            .build());
                        } catch (NumberFormatException e) {
                            LOGGER.warn("Invalid donor/acceptor score value in intron `{}:{}", txAccession, featureInterval);
                        }
                    }
                }

                // assemble transcripts
                for (Map.Entry<String, SplicingTranscript.Builder> entry : txBuilderMap.entrySet()) {
                    // sort and insert exons
                    final Map<Integer, SplicingExon> exonMap = exons.get(entry.getKey());
                    List<SplicingExon> splicingExons = new ArrayList<>(exonMap.size());
                    for (Map.Entry<Integer, SplicingExon> spExEntry : exonMap.entrySet()) {
                        splicingExons.add(spExEntry.getKey(), spExEntry.getValue());
                    }
                    entry.getValue().addAllExons(splicingExons);

                    // sort and insert introns
                    final Map<Integer, SplicingIntron> intronMap = introns.get(entry.getKey());
                    List<SplicingIntron> splicingIntrons = new ArrayList<>(intronMap.size());
                    for (Map.Entry<Integer, SplicingIntron> spInEntry : intronMap.entrySet()) {
                        splicingIntrons.add(spInEntry.getKey(), spInEntry.getValue());
                    }
                    entry.getValue().addAllIntrons(splicingIntrons);
                }

                return txBuilderMap.values().stream()
                        .map(SplicingTranscript.Builder::build)
                        .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            LOGGER.warn("Error fetching data for {}:{}-{}", contig, begin, end, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<SplicingTranscript> fetchTranscriptByAccession(String txAccession, ReferenceDictionary referenceDictionary) {
        String sql = "select tx.CONTIG, " +
                "       tx.BEGIN_POS as tx_begin, " +
                "       tx.END_POS   as tx_end, " +
                "       tx.TX_ACCESSION, " +
                "       tx.STRAND, " +
                "       fr.BEGIN_POS as fr_begin, " +
                "       fr.END_POS   as fr_end, " +
                "       fr.REGION_TYPE, " +
                "       fr.REGION_NUMBER, " +
                "       fr.PROPERTIES " +
                " from SPLICING.TRANSCRIPTS tx " +
                "         left join SPLICING.FEATURE_REGIONS fr on tx.TX_ACCESSION = fr.TX_ACCESSION " +
                "where tx.TX_ACCESSION = ?;";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, txAccession);
            final SplicingTranscript.Builder builder = SplicingTranscript.builder();

            try (ResultSet rs = ps.executeQuery()) {
                boolean txDataParsed = false;
                Map<Integer, SplicingExon> exons = new HashMap<>();
                Map<Integer, SplicingIntron> introns = new HashMap<>();
                // internal ref dict is guaranteed to contain the CONTIG
                while (rs.next()) {

                    String contigName = internalReferenceDictionary.getContigIDToName().get(rs.getInt("CONTIG"));
                    int contigId = referenceDictionary.getContigNameToID().get(contigName);
                    Strand strand = rs.getBoolean("STRAND") ? Strand.FWD : Strand.REV;

                    if (!txDataParsed) {
                        // populate tx-related data
                        if (!referenceDictionary.getContigNameToID().containsKey(contigName)) {
                            LOGGER.warn("Provided reference dictionary does not contain contig `{}` of transcript `{}` ", contigName, txAccession);
                            return Optional.empty();
                        }


                        final GenomeInterval txInterval = new GenomeInterval(referenceDictionary, strand, contigId, rs.getInt("tx_begin"), rs.getInt("tx_end"));
                        builder.setAccessionId(rs.getString("TX_ACCESSION").strip())
                                .setCoordinates(txInterval);
                        txDataParsed = true;
                    }


                    final GenomeInterval featureInterval = new GenomeInterval(referenceDictionary, strand, contigId, rs.getInt("fr_begin"), rs.getInt("fr_end"));

                    final String regType = rs.getString("REGION_TYPE");
                    final int regionNumber = rs.getInt("REGION_NUMBER");

                    if (regType.equals(SplicingTranscript.EXON_REGION_CODE)) {
                        // this SQL record represents exon
                        exons.put(regionNumber,
                                SplicingExon.builder()
                                        .setInterval(featureInterval)
                                        .build());
                    } else if (regType.equals(SplicingTranscript.INTRON_REGION_CODE)) {
                        // this SQL record represents intron
                        try {
                            final String properties = rs.getString("PROPERTIES");
                            final IntronProperties intronProperties = IntronProperties.parseString(properties);

                            introns.put(regionNumber,
                                    SplicingIntron.builder()
                                            .setInterval(featureInterval)
                                            .setDonorScore(intronProperties.getDonorScore())
                                            .setAcceptorScore(intronProperties.getAcceptorScore())
                                            .build());
                        } catch (NumberFormatException e) {
                            LOGGER.warn("Invalid donor/acceptor score value in intron `{}:{}", txAccession, featureInterval);
                        }
                    }
                }
                if (exons.isEmpty() && introns.isEmpty()) {
                    // this happens when txAccession is not in the database
                    return Optional.empty();
                }
                // sort and insert exons
                List<SplicingExon> splicingExons = new ArrayList<>(exons.size());
                for (Map.Entry<Integer, SplicingExon> spExEntry : exons.entrySet()) {
                    splicingExons.add(spExEntry.getKey(), spExEntry.getValue());
                }
                builder.addAllExons(splicingExons);

                // sort and insert introns
                List<SplicingIntron> splicingIntrons = new ArrayList<>(introns.size());
                for (Map.Entry<Integer, SplicingIntron> spInEntry : introns.entrySet()) {
                    splicingIntrons.add(spInEntry.getKey(), spInEntry.getValue());
                }
                builder.addAllIntrons(splicingIntrons);
            }
            return Optional.of(builder.build());
        } catch (SQLException e) {
            LOGGER.warn("Error during query for transcript `{}`", txAccession, e);
            return Optional.empty();
        }
    }

}
