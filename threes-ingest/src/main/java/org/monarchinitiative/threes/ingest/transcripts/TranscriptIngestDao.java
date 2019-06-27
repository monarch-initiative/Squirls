package org.monarchinitiative.threes.ingest.transcripts;

import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingExon;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 *
 */
public class TranscriptIngestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptIngestDao.class);

    private final DataSource dataSource;

    private final GenomeCoordinatesFlipper genomeCoordinatesFlipper;

    public TranscriptIngestDao(DataSource dataSource, GenomeCoordinatesFlipper genomeCoordinatesFlipper) {
        this.dataSource = dataSource;
        this.genomeCoordinatesFlipper = genomeCoordinatesFlipper;
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
        String exonsSql = "INSERT INTO SPLICING.EXONS (TX_ACCESSION, BEGIN_POS, END_POS) " +
                "VALUES (?, ?, ?)";
        String intronsSql = "INSERT INTO SPLICING.INTRONS (TX_ACCESSION, BEGIN_POS, END_POS, DONOR_SCORE, ACCEPTOR_SCORE) " +
                "VALUES (?, ?, ?, ?, ?)";
        int updatedTx = 0;
        int updatedExons = 0, updatedIntrons = 0;


        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement transcripts = connection.prepareStatement(transcriptsSql);
                 PreparedStatement exons = connection.prepareStatement(exonsSql);
                 PreparedStatement introns = connection.prepareStatement(intronsSql)) {

                GenomeCoordinates coordinates = transcript.getTxRegionCoordinates();
                if (!coordinates.getStrand()) {
                    // we need to flip the coordinates to FWD strand
                    final Optional<GenomeCoordinates> flipOp = genomeCoordinatesFlipper.flip(coordinates);
                    if (!flipOp.isPresent()) {
                        // complaint made in coordinates flipper
                        return 0;
                    }
                    coordinates = flipOp.get();
                }

                transcripts.setString(1, transcript.getContig());
                transcripts.setInt(2, transcript.getTxBegin());
                transcripts.setInt(3, transcript.getTxEnd());
                transcripts.setInt(4, coordinates.getBegin());
                transcripts.setInt(5, coordinates.getEnd());
                transcripts.setBoolean(6, transcript.getStrand());
                transcripts.setString(7, transcript.getAccessionId());
                updatedTx += transcripts.executeUpdate();

                for (SplicingExon exon : transcript.getExons()) {
                    exons.setString(1, transcript.getAccessionId());
                    exons.setInt(2, exon.getBegin());
                    exons.setInt(3, exon.getEnd());
                    updatedExons += exons.executeUpdate();
                }

                for (SplicingIntron intron : transcript.getIntrons()) {
                    introns.setString(1, transcript.getAccessionId());
                    introns.setInt(2, intron.getBegin());
                    introns.setInt(3, intron.getEnd());
                    introns.setDouble(4, intron.getDonorScore());
                    introns.setDouble(5, intron.getAcceptorScore());
                    updatedIntrons += introns.executeUpdate();
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
