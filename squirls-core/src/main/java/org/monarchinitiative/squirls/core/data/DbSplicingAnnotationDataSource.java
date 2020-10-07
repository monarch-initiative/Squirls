package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.FloatRegion;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.squirls.core.scoring.TrackRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DbSplicingAnnotationDataSource extends BaseDao implements SplicingAnnotationDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSplicingAnnotationDataSource.class);

    /**
     * Number of upstream and downstream PhyloP scores fetched for a query region.
     */
    private static final int PHYLOP_PADDING_N = 2;

    private static final String FASTA_TRACK_NAME = "fasta";
    private static final String PHYLOP_TRACK_NAME = "phylop";

    public DbSplicingAnnotationDataSource(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ReferenceDictionary getReferenceDictionary() {
        return internalReferenceDictionary;
    }

    @Override
    public Set<String> getTranscriptAccessionIds() {
        String txSql = "select distinct ACCESSION_ID from SPLICING.TRANSCRIPT";

        Set<String> accessions = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement ps = connection.prepareStatement(txSql);
                 final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accessions.add(rs.getString("ACCESSION_ID"));
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }
        return accessions;
    }

    @Override
    public Map<String, SplicingAnnotationData> getAnnotationData(String contig, int begin, int end) {
        if (!internalReferenceDictionary.getContigNameToID().containsKey(contig)) {
            LOGGER.warn("Contig `{}` is not represented in database", contig);
            return Map.of();
        }
        final int dbChr = internalReferenceDictionary.getContigNameToID().get(contig);
        final GenomeInterval queryInterval = new GenomeInterval(internalReferenceDictionary, Strand.FWD, dbChr, begin, end);

        // transcript data
        String txSql = "select tx.TX_ID tx_id, tx.ACCESSION_ID, tx.CONTIG as tx_contig, tx.BEGIN_POS tx_begin, tx.END_POS tx_end, tx.STRAND tx_strand, " +
                // exons & introns data
                "  txf.TX_ID txf_id, txf.BEGIN_POS f_begin, txf.END_POS f_end, txf.REGION_NUMBER r_nmb, txf.REGION_TYPE r_tp, txf.PROPERTIES " +
                "from SPLICING.TRANSCRIPT tx " +
                "  join SPLICING.TX_FEATURE_REGION txf on tx.TX_ID = txf.TX_ID " +
                "where tx.CONTIG = ? " +
                "  and ? < tx.END_ON_FWD " +
                "  and tx.BEGIN_ON_FWD < ?";

        String tracksSql = "select tx.ACCESSION_ID tx_accession, g.SYMBOL symbol, " +
                "  t.CONTIG t_contig, t.BEGIN_POS t_begin, t.END_POS t_end, t.STRAND t_strand, " +
                "  t.FASTA_SEQUENCE fasta, t.PHYLOP_VALUES phylop " +
                "from SPLICING.GENE g " +
                "  join SPLICING.GENE_TO_TX gtx on g.GENE_ID = gtx.GENE_ID " +
                "  join SPLICING.TRANSCRIPT tx on gtx.TX_ID = tx.TX_ID " +
                "  join SPLICING.GENE_TRACK t on g.GENE_ID = t.GENE_ID " +
                "where g.CONTIG = ? " +
                "  and ? < g.END_ON_FWD " +
                "  and g.BEGIN_ON_FWD < ?";

        /*
         We want to populate these collections:
         */
        final Set<SplicingTranscript> transcripts = new HashSet<>();

        // key - gene symbol
        //   key - track name, value - track
        final Map<String, Map<String, TrackRegion<?>>> trackMap = new HashMap<>();
        // key - gene symbol (e.g. HNF4A), value - set of transcript accession IDs (e.g. NM_175914.4)
        final Map<String, Set<String>> geneToTxMap = new HashMap<>();


        try (Connection connection = dataSource.getConnection();
             PreparedStatement txPs = connection.prepareStatement(txSql);
             PreparedStatement tracksPs = connection.prepareStatement(tracksSql)) {
            /*
                PROCESS TRANSCRIPTS
             */
            txPs.setInt(1, dbChr);
            txPs.setInt(2, begin);
            txPs.setInt(3, end);

            try (ResultSet rs = txPs.executeQuery()) {
                // populate the maps using data from the result set
                transcripts.addAll(assembleTranscripts(rs));
            }

            /*
                PROCESS GENE TRACKS
             */
            tracksPs.setInt(1, dbChr);
            tracksPs.setInt(2, begin);
            tracksPs.setInt(3, end);

            try (ResultSet rs = tracksPs.executeQuery()) {
                final TrackData trackData = assembleTracks(rs, queryInterval);

                trackMap.putAll(trackData.trackMap);
                geneToTxMap.putAll(trackData.geneToTxMap);
            }

        } catch (SQLException e) {
            LOGGER.warn("Error: ", e);
        }

        // now let's assemble the result
        final Map<String, SplicingAnnotationData> anns = new HashMap<>();

        final Map<String, SplicingTranscript> txByAccession = transcripts.stream()
                .collect(Collectors.toMap(SplicingTranscript::getAccessionId, Function.identity()));

        for (String symbol : geneToTxMap.keySet()) {
            // gather transcripts for this gene
            final Set<SplicingTranscript> geneTranscripts = geneToTxMap.get(symbol).stream()
                    .map(txByAccession::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            // gather tracks that belong to the gene
            final Map<String, TrackRegion<?>> tracks = trackMap.get(symbol);

            anns.put(symbol, SplicingAnnotationDataImpl.of(geneTranscripts, tracks));
        }

        return anns;
    }

    private Collection<SplicingTranscript> assembleTranscripts(ResultSet rs) throws SQLException {

        Map<Integer, SplicingTranscript.Builder> txBuilderMap = new HashMap<>();
        Map<Integer, Map<Integer, SplicingExon>> exons = new HashMap<>();
        Map<Integer, Map<Integer, SplicingIntron>> introns = new HashMap<>();
        while (rs.next()) {
            final int txId = rs.getInt("tx_id");
            final String txAccession = rs.getString("accession_id");
            final int txContig = rs.getInt("tx_contig");
            final int txBegin = rs.getInt("tx_begin");
            final int txEnd = rs.getInt("tx_end");
            final Strand strand = rs.getBoolean("tx_strand") ? Strand.FWD : Strand.REV;
            if (!txBuilderMap.containsKey(txId)) {
                        /*
                        this is the first time we see data regarding the transcript with `txId`,
                        we need to add transcript-related data, such as accession and interval
                         */
                final GenomeInterval txInterval = new GenomeInterval(internalReferenceDictionary, strand, txContig, txBegin, txEnd);
                txBuilderMap.put(txId, SplicingTranscript.builder()
                        .setAccessionId(txAccession)
                        .setCoordinates(txInterval));
                exons.put(txId, new HashMap<>());
                introns.put(txId, new HashMap<>());
            }
            int fBegin = rs.getInt("f_begin"), fEnd = rs.getInt("f_end");
            final GenomeInterval featureInterval = new GenomeInterval(internalReferenceDictionary, strand, txContig, fBegin, fEnd);

            final String regType = rs.getString("r_tp");
            final int regionNumber = rs.getInt("r_nmb");

            if (regType.equals(SplicingTranscript.EXON_REGION_CODE)) {
                // this SQL record represents exon
                exons.get(txId).put(regionNumber,
                        SplicingExon.builder()
                                .setInterval(featureInterval)
                                .build());
            } else if (regType.equals(SplicingTranscript.INTRON_REGION_CODE)) {
                // this SQL record represents intron
                try {
                    final String properties = rs.getString("PROPERTIES");
                    final IntronProperties intronProperties = IntronProperties.parseString(properties);

                    introns.get(txId).put(regionNumber,
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
        for (Map.Entry<Integer, SplicingTranscript.Builder> entry : txBuilderMap.entrySet()) {
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
                .collect(Collectors.toSet());
    }

    private TrackData assembleTracks(ResultSet rs, GenomeInterval query) throws SQLException {
        // key - gene symbol
        //   key - track name, value - track
        Map<String, Map<String, TrackRegion<?>>> trackMap = new HashMap<>();
        // key - gene symbol (e.g. HNF4A), value - set of transcript accession IDs (e.g. NM_175914.4)
        Map<String, Set<String>> geneToTxMap = new HashMap<>();

        while (rs.next()) {
            final String symbol = rs.getString("symbol");
            if (!trackMap.containsKey(symbol)) {
                /*
                 We're seeing this gene symbol for the first time.
                 This is the time to add tracks, since tracks are unique to the gene
                 */
                Map<String, TrackRegion<?>> tracks = new HashMap<>();
                // 0 - figure out track's interval
                final int trackContig = rs.getInt("t_contig");
                final int trackBegin = rs.getInt("t_begin");
                final int trackEnd = rs.getInt("t_end");
                final Strand trackStrand = rs.getBoolean("t_strand") ? Strand.FWD : Strand.REV;
                final GenomeInterval trackInterval = new GenomeInterval(internalReferenceDictionary, trackStrand, trackContig, trackBegin, trackEnd);
                final GenomeInterval queryOnStrand = query.withStrand(trackStrand);

                // 1 - decode fasta track
                /*
                 The track is stored in the database as
                 */
                tracks.put(FASTA_TRACK_NAME, SequenceRegion.of(trackInterval, rs.getBytes("fasta")));

                // 2 - decode phylop track with float score per position
                /*
                 The track is stored in the database in form of a *4-long array (n=trackInterval.length()) where each
                 float is encoded as 4 bytes.
                 To save memory, we only get the "relevant" (target) part of the PhyloP track.
                 */
                final int targetLength = (queryOnStrand.length() + (PHYLOP_PADDING_N * 2));
                final int beginOffset = (queryOnStrand.getBeginPos() - trackInterval.getBeginPos()) - PHYLOP_PADDING_N;
                // in input stream, the data interesting for this query begins at this offset
                final int byteOffset = beginOffset * 4; // 4 <- n bytes per float
                final GenomeInterval target = new GenomeInterval(new GenomePosition(internalReferenceDictionary, trackStrand, trackContig, trackBegin + beginOffset), targetLength);
                if (trackInterval.contains(target)) {

                    try (final DataInputStream phylopStream = new DataInputStream(rs.getBinaryStream("phylop"))) {
                        // first, let's get to the target region
                        long skipped = phylopStream.skip(byteOffset);
                        if (skipped == byteOffset) {
                            // now we are at the right place to start reading target data
                            try {
                                float[] phylopValues = new float[targetLength];
                                int i = 0;
                                while (i < targetLength) {
                                    phylopValues[i] = phylopStream.readFloat();
                                    i++;
                                }
                                tracks.put(PHYLOP_TRACK_NAME, FloatRegion.of(target, phylopValues));
                            } catch (EOFException eof) {
                                // stream ended before finishing reading the target region
                                LOGGER.warn("Not enough phylop track available for query {}", target);
                            }
                        } else {
                            // stream ended before reaching the target region
                            LOGGER.warn("Not enough phylop track available for query. Track ended at {} before reaching target region {}",
                                    new GenomePosition(internalReferenceDictionary, trackStrand, trackContig, (int) (trackBegin + skipped)), target);
                        }
                    } catch (IOException e) {
                        LOGGER.warn("Error decoding phylop track ({}:{}-{}) for {}", trackContig, trackBegin, trackEnd, symbol, e);
                    }
                } else {
                    LOGGER.warn("Cannot get phylop track for {} using {}", target, trackInterval);
                }
                trackMap.put(symbol, tracks);
                geneToTxMap.put(symbol, new HashSet<>());
            }
            // now we store the gene-transcript pair
            final String txAccession = rs.getString("tx_accession");
            geneToTxMap.get(symbol).add(txAccession);
        }
        return new TrackData(trackMap, geneToTxMap);
    }

    private static class TrackData {

        private final Map<String, Map<String, TrackRegion<?>>> trackMap;
        private final Map<String, Set<String>> geneToTxMap;

        private TrackData(Map<String, Map<String, TrackRegion<?>>> trackMap, Map<String, Set<String>> geneToTxMap) {
            this.trackMap = trackMap;
            this.geneToTxMap = geneToTxMap;
        }
    }
}
