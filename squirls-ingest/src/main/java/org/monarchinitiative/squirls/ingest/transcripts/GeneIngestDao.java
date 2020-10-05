package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This class inserts data belonging to a gene into the database within a single transaction.
 */
public class GeneIngestDao {

    static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneIngestDao.class);
    private final DataSource dataSource;

    private final ReferenceDictionary rd;

    private final AtomicInteger geneIdx, txIdx;

    public GeneIngestDao(DataSource dataSource, ReferenceDictionary rd) {
        this.dataSource = dataSource;
        this.rd = rd;
        geneIdx = new AtomicInteger(0);
        txIdx = new AtomicInteger(0);
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

    /**
     * Convert list of floats into a byte array. Each float occupies 4 bytes of space in the array.
     *
     * @param values list of floats
     * @return byte array with length of multiple of 4 where each 4 bytes represent a float from the input {@code values}
     */
    private static byte[] encodeFloatsToBytes(float[] values) {
        byte[] arr = new byte[values.length * 4];
        for (int i = 0; i < values.length; i++) {
            // convert float into 4 bytes
            byte[] current = ByteBuffer.allocate(4).putFloat(values[i]).array();
            // put the bytes into the array
            System.arraycopy(current, 0, arr, i * 4, 4);
        }
        return arr;
    }

    private static byte[] encodeStringToBytes(String data) {
        return data.getBytes(CHARSET);
    }

    /**
     * Store the gene data into the database.
     *
     * @param data representing gene
     * @return number of affected rows
     */
    public int insertGene(GeneAnnotationData data) {
        int updated = 0;
        /*
         * Let's perform sanity checks first
         */
        if (data == null) {
            LOGGER.warn("Refusing to insert null to database");
            return updated;
        }

        if (data.getTranscripts().isEmpty()) {
            LOGGER.warn("Refusing to store gene `{}` with 0 transcripts", data.getSymbol());
            return updated;
        }

        final Set<Integer> contigIds = data.getTranscripts().stream().map(SplicingTranscript::getChr).collect(Collectors.toSet());
        if (contigIds.size() != 1) {
            LOGGER.warn("Refusing to store gene {} where transcripts are on multiple contigs: `{}`", data.getSymbol(), contigIds);
            return updated;
        }

        /*
        Now let's insert the data into the database. Data is inserted into the following tables:
         - SPLICING.GENE
         - SPLICING.GENE_TRACK
         - SPLICING.GENE_TO_TX
         - SPLICING.TRANSCRIPT
         - SPLICING.TX_FEATURE_REGION
         */
        /*
        SQL statements:
         */
        String genesSql = "insert into SPLICING.GENE(CONTIG, BEGIN_POS, END_POS, " +
                "BEGIN_ON_FWD, END_ON_FWD, STRAND, " +
                "GENE_ID, SYMBOL) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";

        String geneTracksSql = "insert into SPLICING.GENE_TRACK(GENE_ID, " +
                "CONTIG, BEGIN_POS, END_POS, STRAND, " +
                "FASTA_SEQUENCE, PHYLOP_VALUES) " +
                "values (?, ?, ?, ?, ?, ?, ?)";

        String geneToTxSql = "insert into SPLICING.GENE_TO_TX(GENE_ID, TX_ID) " +
                "values (?, ?)";

        String transcriptsSql = "insert into SPLICING.TRANSCRIPT (TX_ID, CONTIG, BEGIN_POS, END_POS, " +
                "BEGIN_ON_FWD, END_ON_FWD, STRAND, ACCESSION_ID) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";

        String featureRegionsSql = "insert into SPLICING.TX_FEATURE_REGION " +
                " (TX_ID, CONTIG, BEGIN_POS, END_POS, " +
                "REGION_TYPE, REGION_NUMBER, PROPERTIES) " +
                "values ( ?, ?, ?, ?, ?, ?, ?)";


        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (
                    final PreparedStatement genesPs = connection.prepareStatement(genesSql);
                    final PreparedStatement geneTracksPs = connection.prepareStatement(geneTracksSql);
                    final PreparedStatement geneToTxPs = connection.prepareStatement(geneToTxSql);
                    final PreparedStatement transcriptPs = connection.prepareStatement(transcriptsSql);
                    final PreparedStatement featureRegionsPs = connection.prepareStatement(featureRegionsSql)
            ) {
                // unique gene id within the database
                final int geneId = geneIdx.getAndIncrement();

                //noinspection OptionalGetWithoutIsPresent we check for this upstream
                final SplicingTranscript firstTx = data.getTranscripts().stream().findFirst().get();
                final int contigId = rd.getContigNameToID().get(firstTx.getChrName());
                //noinspection OptionalGetWithoutIsPresent we check for this upstream
                final GenomeInterval geneRegion = Utils.getGeneRegionFromSplicingTranscripts(data.getTranscripts()).get();
                final GenomeInterval geneRegionOnFwd = geneRegion.withStrand(Strand.FWD);

                // insert into SPLICING.GENE
                // (CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, GENE_ID, SYMBOL) ...
                genesPs.setInt(1, contigId);
                genesPs.setInt(2, geneRegion.getBeginPos());
                genesPs.setInt(3, geneRegion.getEndPos());
                genesPs.setInt(4, geneRegionOnFwd.getBeginPos());
                genesPs.setInt(5, geneRegionOnFwd.getEndPos());
                genesPs.setBoolean(6, geneRegion.getStrand().isForward());
                genesPs.setInt(7, geneId);
                genesPs.setString(8, data.getSymbol());
                int genesUpdated = genesPs.executeUpdate();

                final GenomeInterval trackInterval = data.getTrackInterval();
                final int trackContigId = rd.getContigNameToID().get(trackInterval.getRefDict().getContigIDToName().get(trackInterval.getChr()));
                // insert into SPLICING.GENE_TRACK
                // (GENE_ID, CONTIG, BEGIN_POS, END_POS, STRAND, FASTA_SEQUENCE, PHYLOP_VALUES) ...
                geneTracksPs.setInt(1, geneId);
                geneTracksPs.setInt(2, trackContigId);
                geneTracksPs.setInt(3, trackInterval.getBeginPos());
                geneTracksPs.setInt(4, trackInterval.getEndPos());
                geneTracksPs.setBoolean(5, trackInterval.getStrand().isForward());
                geneTracksPs.setBytes(6, encodeStringToBytes(data.getRefSequence()));
                geneTracksPs.setBytes(7, encodeFloatsToBytes(data.getPhylopScores()));
                int tracksUpdated = geneTracksPs.executeUpdate();

                int geneToTx = 0;
                int updatedTx = 0;
                int updatedExons = 0;
                int updatedIntrons = 0;
                for (SplicingTranscript tx : data.getTranscripts()) {
                    /*
                     *        INSERT TRANSCRIPT, EXONS, INTRONS
                     */
                    // transcript id unique within the database
                    final int txId = txIdx.getAndIncrement();

                    // insert into SPLICING.GENE_TO_TX(GENE_ID, TX_ID) ...
                    geneToTxPs.setInt(1, geneId);
                    geneToTxPs.setInt(2, txId);
                    geneToTx += geneToTxPs.executeUpdate();

                    final int internalContigId = rd.getContigNameToID().get(tx.getChrName());
                    final GenomeInterval txOnFwd = tx.getTxRegionCoordinates().withStrand(Strand.FWD);

                    // insert into SPLICING.TRANSCRIPT
                    // (TX_ID, CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, ACCESSION_ID) ...
                    transcriptPs.setInt(1, txId);
                    transcriptPs.setInt(2, internalContigId);
                    transcriptPs.setInt(3, tx.getTxBegin());
                    transcriptPs.setInt(4, tx.getTxEnd());
                    transcriptPs.setInt(5, txOnFwd.getBeginPos());
                    transcriptPs.setInt(6, txOnFwd.getEndPos());
                    transcriptPs.setBoolean(7, tx.getStrand().isForward());
                    transcriptPs.setString(8, tx.getAccessionId());
                    updatedTx += transcriptPs.executeUpdate();

                    // insert exons
                    for (int i = 0; i < tx.getExons().size(); i++) {
                        final SplicingExon exon = tx.getExons().get(i);
                        final GenomeInterval interval = exon.getInterval();

                        // insert into SPLICING.TX_FEATURE_REGION
                        // (TX_ID, CONTIG, BEGIN_POS, END_POS, REGION_TYPE, REGION_NUMBER, PROPERTIES) ...
                        featureRegionsPs.setInt(1, txId);
                        featureRegionsPs.setInt(2, internalContigId);
                        featureRegionsPs.setInt(3, interval.getBeginPos());
                        featureRegionsPs.setInt(4, interval.getEndPos());
                        featureRegionsPs.setString(5, SplicingTranscript.EXON_REGION_CODE);
                        featureRegionsPs.setInt(6, i);
                        featureRegionsPs.setString(7, getExonProperties(exon));

                        updatedExons += featureRegionsPs.executeUpdate();
                    }

                    // insert introns
                    for (int i = 0; i < tx.getIntrons().size(); i++) {
                        final SplicingIntron intron = tx.getIntrons().get(i);
                        final GenomeInterval interval = intron.getInterval();

                        // insert into SPLICING.TX_FEATURE_REGION
                        // (TX_ID, CONTIG, BEGIN_POS, END_POS, REGION_TYPE, REGION_NUMBER, PROPERTIES) ...
                        featureRegionsPs.setInt(1, txId);
                        featureRegionsPs.setInt(2, internalContigId);
                        featureRegionsPs.setInt(3, interval.getBeginPos());
                        featureRegionsPs.setInt(4, interval.getEndPos());
                        featureRegionsPs.setString(5, SplicingTranscript.INTRON_REGION_CODE);
                        featureRegionsPs.setInt(6, i);
                        featureRegionsPs.setString(7, getIntronProperties(intron));
                        updatedIntrons += featureRegionsPs.executeUpdate();
                    }
                }
                connection.commit();
                updated += genesUpdated + tracksUpdated + geneToTx + updatedTx + updatedExons + updatedIntrons;
            } catch (SQLException e) {
                LOGGER.warn("Error occurred during update, rolling back", e);
                connection.rollback();
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.warn("Error occurred", e);
        }
        return updated;
    }
}
