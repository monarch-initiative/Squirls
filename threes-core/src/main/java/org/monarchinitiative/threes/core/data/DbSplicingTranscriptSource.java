package org.monarchinitiative.threes.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
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
