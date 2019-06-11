package org.monarchinitiative.sss.core.data;

import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SplicingExon;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.reference.fasta.InvalidCoordinatesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class DbSplicingTranscriptSource implements SplicingTranscriptSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSplicingTranscriptSource.class);

    private final DataSource dataSource;

    private final Map<String, Integer> contigLengthMap;

    public DbSplicingTranscriptSource(DataSource dataSource, Map<String, Integer> contigLengthMap) {
        this.dataSource = dataSource;
        this.contigLengthMap = contigLengthMap;
    }


    @Override
    public List<SplicingTranscript> fetchTranscripts(String contig, int begin, int end) {
        String exonsSql = "SELECT st.contig, st.begin_pos, st.end_pos, st.strand, st.tx_accession, " +
                "se.begin_pos as eb, se.end_pos as ee " +
                "FROM splicing.transcripts st " +
                "INNER JOIN splicing.exons se ON st.tx_accession = se.tx_accession " +
                "WHERE st.contig = ? and st.begin_on_fwd < ? and ? < st.end_on_fwd";

        String intronsSql = "SELECT st.tx_accession, si.begin_pos, si.end_pos, si.donor_score, si.acceptor_score " +
                "FROM splicing.transcripts st " +
                "INNER JOIN splicing.introns si ON st.tx_accession = si.tx_accession " +
                "WHERE st.contig = ? and st.begin_on_fwd < ? and ? < st.end_on_fwd";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement exonsSt = connection.prepareStatement(exonsSql);
             final PreparedStatement intronsSt = connection.prepareStatement(intronsSql)) {

            // -------------    FETCH EXONS     -------------
            exonsSt.setString(1, contig);
            exonsSt.setInt(2, end);
            exonsSt.setInt(3, begin);

            final ResultSet exonsRs = exonsSt.executeQuery();
            Map<String, SplicingTranscript.Builder> transcriptMap = new HashMap<>();

            while (exonsRs.next()) {
                final String accession = exonsRs.getString("tx_accession");

                if (!transcriptMap.containsKey(accession)) {
                    final String tContig = exonsRs.getString("contig");
                    final int tBegin = exonsRs.getInt("begin_pos");
                    final int tEnd = exonsRs.getInt("end_pos");
                    final boolean tStrand = exonsRs.getBoolean("strand");
                    transcriptMap.put(accession, SplicingTranscript.newBuilder()
                            .setAccessionId(accession)
                            .setCoordinates(GenomeCoordinates.newBuilder()
                                    .setContig(tContig)
                                    .setBegin(tBegin)
                                    .setEnd(tEnd)
                                    .setStrand(tStrand)
                                    .build())
                    );
                }

                int eBegin = exonsRs.getInt("eb");
                int eEnd = exonsRs.getInt("ee");
                transcriptMap.get(accession).addExon(SplicingExon.newBuilder()
                        .setBegin(eBegin)
                        .setEnd(eEnd)
                        .build());
            }

            // -------------    FETCH INTRONS    -------------
            intronsSt.setString(1, contig);
            intronsSt.setInt(2, end);
            intronsSt.setInt(3, begin);
            final ResultSet intronsRs = intronsSt.executeQuery();
            while (intronsRs.next()) {
                final String accession = intronsRs.getString("tx_accession");
                transcriptMap.get(accession)
                        .addIntron(SplicingIntron.newBuilder()
                                .setBegin(intronsRs.getInt("begin_pos"))
                                .setEnd(intronsRs.getInt("end_pos"))
                                .setDonorScore(intronsRs.getDouble("donor_score"))
                                .setAcceptorScore(intronsRs.getDouble("acceptor_score"))
                                .build());
            }

            return transcriptMap.values().stream()
                    .map(SplicingTranscript.Builder::build)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            LOGGER.warn("Error fetching data for {}:{}-{}", contig, begin, end, e);
        } catch (InvalidCoordinatesException e) {
            // this should not happen
            LOGGER.error("There was transcript with invalid coordinates in query {}:{}-{}", contig, begin, end, e);
        }
        return Collections.emptyList();
    }

}
