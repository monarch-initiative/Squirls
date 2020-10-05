package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.ingest.ProgressLogger;
import org.monarchinitiative.squirls.ingest.SquirlsDataBuilder;
import org.monarchinitiative.squirls.ingest.conservation.BigWigAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class TranscriptsIngestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptsIngestRunner.class);

    private final SplicingCalculator calculator;

    private final GeneIngestDao dao;

    private final GenomeSequenceAccessor genomeSequenceAccessor;

    private final BigWigAccessor phylopAccessor;

    public TranscriptsIngestRunner(DataSource dataSource,
                                   ReferenceDictionary rd,
                                   SplicingCalculator calculator,
                                   GenomeSequenceAccessor genomeSequenceAccessor,
                                   BigWigAccessor phylopAccessor) {
        this.calculator = calculator;
        this.dao = new GeneIngestDao(dataSource, rd);
        this.genomeSequenceAccessor = genomeSequenceAccessor;
        this.phylopAccessor = phylopAccessor;
    }

    public void run(Collection<TranscriptModel> transcripts) {
        // group transcripts by gene symbol
        final Map<String, List<TranscriptModel>> txByGeneSymbol = transcripts.stream()
                .collect(Collectors.groupingBy(TranscriptModel::getGeneSymbol));
        LOGGER.info("Processing {} genes", txByGeneSymbol.size());

        int updated = 0;
        ProgressLogger progress = new ProgressLogger();

        for (String symbol : txByGeneSymbol.keySet()) {
            final List<TranscriptModel> txs = txByGeneSymbol.get(symbol);
            final Optional<GenomeInterval> boundaries = Utils.getGeneBoundariesFromTranscriptModel(txs);
            if (boundaries.isEmpty()) {
                // no transcript for the gene
                LOGGER.warn("No tx found for gene `{}`", symbol);
                continue;
            }

            /*
              we're interested in fetching reference sequence and PhyloP scores for this interval
            */
            final GenomeInterval interval = boundaries.get().withMorePadding(SquirlsDataBuilder.GENE_SEQUENCE_PADDING);
            // Sequence
            final Optional<SequenceInterval> opt = genomeSequenceAccessor.fetchSequence(interval);
            if (opt.isEmpty()) {
                LOGGER.warn("Could not fetch sequence for gene {} at {}", symbol, interval);
                continue;
            }
            final String sequence = opt.get().getSequence();

            // PhyloP
            final float[] phylopScores = phylopAccessor.getScores(interval);

            // Calculate information content for transcripts
            final List<SplicingTranscript> stxs = txs.stream()
                    .map(calculator::calculate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            final GeneAnnotationData data = new GeneAnnotationData(symbol, stxs, interval, sequence, phylopScores);
            updated += dao.insertGene(data);
            progress.logTotal("Processed {} genes");
        }
        LOGGER.info("Updated {} rows", updated);
    }
}
