package org.monarchinitiative.threes.ingest.transcripts;

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
import java.sql.SQLException;

/**
 *
 */
public class TranscriptIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptIngestDao.class);

    private final DataSource dataSource;

    private final ReferenceDictionary referenceDictionary;

    public TranscriptIngestDao(DataSource dataSource, ReferenceDictionary referenceDictionary) {
        this.dataSource = dataSource;
        this.referenceDictionary = referenceDictionary;
    }

    /**
     * We do not currently store anything for exon.
     *
     * @param exon Splicing exon
     * @return empty string at the moment
     */
    private static String getExonProperties(SplicingExon exon) {
        return "";
    }

    private static String getIntronProperties(SplicingIntron intron) {
        return "DONOR=" + intron.getDonorScore() + ";" +
                "ACCEPTOR=" + intron.getAcceptorScore();

    }

    public int insertTranscript(SplicingTranscript transcript) {
        if (transcript == null) {
            LOGGER.warn("Refusing to insert null to database");
            return 0;
        } else if (transcript.equals(SplicingTranscript.getDefaultInstance())) {
            LOGGER.warn("Refusing to insert empty data to database");
            return 0;
        }

        String transcriptsSql = "INSERT INTO SPLICING.TRANSCRIPTS (CONTIG, BEGIN_POS, END_POS, " +
                "BEGIN_ON_FWD, END_ON_FWD, STRAND, TX_ACCESSION) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String featureRegionsSql = "insert into SPLICING.FEATURE_REGIONS " +
                " (CONTIG, BEGIN_POS, END_POS, " +
                " TX_ACCESSION, REGION_TYPE, PROPERTIES, REGION_NUMBER) " +
                " values ( ?, ?, ?, ?, ?, ?, ?)";
        int updatedTx = 0;
        int updatedExons = 0, updatedIntrons = 0;


        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (final PreparedStatement transcriptPs = connection.prepareStatement(transcriptsSql);
                 final PreparedStatement featureRegionsPs = connection.prepareStatement(featureRegionsSql)) {

                final GenomeInterval txOnFwd = transcript.getTxRegionCoordinates().withStrand(Strand.FWD);
                final String internalContigName = referenceDictionary.getContigIDToName().get(transcript.getChr());
                final Integer internalContigId = referenceDictionary.getContigNameToID().get(internalContigName);

                // insert transcript
                transcriptPs.setInt(1, internalContigId);
                transcriptPs.setInt(2, transcript.getTxBegin());
                transcriptPs.setInt(3, transcript.getTxEnd());
                transcriptPs.setInt(4, txOnFwd.getBeginPos());
                transcriptPs.setInt(5, txOnFwd.getEndPos());
                transcriptPs.setBoolean(6, transcript.getStrand().isForward());
                transcriptPs.setString(7, transcript.getAccessionId());
                updatedTx += transcriptPs.executeUpdate();

                // insert exons
                for (int i = 0; i < transcript.getExons().size(); i++) {
                    final SplicingExon exon = transcript.getExons().get(i);
                    final GenomeInterval interval = exon.getInterval();

                    featureRegionsPs.setInt(1, internalContigId);
                    featureRegionsPs.setInt(2, interval.getBeginPos());
                    featureRegionsPs.setInt(3, interval.getEndPos());
                    featureRegionsPs.setString(4, transcript.getAccessionId());
                    featureRegionsPs.setString(5, SplicingTranscript.EXON_REGION_CODE);
                    featureRegionsPs.setString(6, getExonProperties(exon));
                    featureRegionsPs.setInt(7, i);

                    updatedExons += featureRegionsPs.executeUpdate();
                }

                // insert introns
                for (int i = 0; i < transcript.getIntrons().size(); i++) {
                    final SplicingIntron intron = transcript.getIntrons().get(i);
                    final GenomeInterval interval = intron.getInterval();

                    featureRegionsPs.setInt(1, internalContigId);
                    featureRegionsPs.setInt(2, interval.getBeginPos());
                    featureRegionsPs.setInt(3, interval.getEndPos());
                    featureRegionsPs.setString(4, transcript.getAccessionId());
                    featureRegionsPs.setString(5, SplicingTranscript.INTRON_REGION_CODE);
                    featureRegionsPs.setString(6, getIntronProperties(intron));
                    featureRegionsPs.setInt(7, i);
                    updatedIntrons += featureRegionsPs.executeUpdate();
                }


                connection.commit();
            } catch (SQLException e) {
                LOGGER.warn("Error occured during update, rolling back", e);
                connection.rollback();
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.warn("Error occurred", e);
        }
        LOGGER.debug("Inserted {} exons and {} introns for tx {}", updatedExons, updatedIntrons, transcript.getAccessionId());
        return updatedTx;
    }
}
