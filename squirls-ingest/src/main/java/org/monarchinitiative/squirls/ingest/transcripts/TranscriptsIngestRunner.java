package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;

/**
 *
 */
public class TranscriptsIngestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptsIngestRunner.class);

    private final ReferenceDictionary rd;

    private final SplicingCalculator calculator;

    private final GeneIngestDao dao;

    private final GenomeSequenceAccessor genomeSequenceAccessor;

    private final BigWigAccessor phylopAccessor;

    public TranscriptsIngestRunner(DataSource dataSource,
                                   ReferenceDictionary rd,
                                   SplicingCalculator calculator,
                                   GenomeSequenceAccessor genomeSequenceAccessor,
                                   BigWigAccessor phylopAccessor) {
        this.rd = rd;
        this.calculator = calculator;
        this.dao = new GeneIngestDao(dataSource, rd);
        this.genomeSequenceAccessor = genomeSequenceAccessor;
        this.phylopAccessor = phylopAccessor;
    }

    private static Function<Map.Entry<String, List<TranscriptModel>>, Optional<GeneAnnotationData>> toGeneAnnotationData(GenomeSequenceAccessor genomeSequenceAccessor,
                                                                                                                         BigWigAccessor phylopAccessor,
                                                                                                                         SplicingCalculator calculator,
                                                                                                                         ReferenceDictionary rd) {
        return entry -> {
            final String symbol = entry.getKey();
            final List<TranscriptModel> txs = entry.getValue().stream()
                    .filter(transcriptsOnNonPrimaryContigs())
                    .filter(transcriptsOnUnknownContigs(rd))
                    .collect(toList());

            if (txs.isEmpty()) {
                // all transcripts were located on non-primary chromosomes
                return Optional.empty();
            }

            final Set<String> contigs = txs.stream()
                    // jannovar's frakking reference dictionary is freaking awesome!
                    .map(tm -> tm.getTXRegion().getRefDict().getContigIDToName().get(tm.getChr()))
                    .map(contig -> contig.startsWith("chr") ? contig.substring(3) : contig) // get rid of `chr`
                    .collect(toSet());

            if (contigs.size() != 1) {
                if (contigs.contains("X") && contigs.contains("Y")) {
                    // handle the special case when transcripts are distributed on chrX and chrY,
                    // we keep transcripts located on chrX
                    final List<TranscriptModel> transcripts = new ArrayList<>();
                    for (final TranscriptModel tm : txs) {
                        if (tm.getTXRegion().getRefDict().getContigIDToName().get(tm.getChr()).matches("^(chr)?X$")) {
                            transcripts.add(tm);
                        }
                    }
                    LOGGER.info("Gene {} has transcripts on {}. Keeping transcripts {} located on chrX", symbol, contigs, transcripts.stream().map(TranscriptModel::getAccession).sorted().collect(toList()));
                    txs.clear();
                    txs.addAll(transcripts);
                } else {
                    LOGGER.warn("Gene {} has transcripts on multiple contigs: {}, {}", symbol, contigs, txs.stream().map(TranscriptModel::getAccession).sorted().collect(toList()));
                    return Optional.empty();
                }
            }

            final Optional<GenomeInterval> boundaries = Utils.getGeneBoundariesFromTranscriptModel(txs);
            if (boundaries.isEmpty()) {
                // no transcript for the gene
                LOGGER.warn("No tx found for gene `{}`", symbol);
                return Optional.empty();
            }

            /*
              we're interested in fetching reference sequence and PhyloP scores for this interval
            */
            final GenomeInterval bnd = boundaries.get();
            final GenomeInterval bndOnBuild = new GenomeInterval(rd, bnd.getStrand(),
                    rd.getContigNameToID().get(bnd.getRefDict().getContigIDToName().get(bnd.getChr())),
                    bnd.getBeginPos(), bnd.getEndPos());

            final int contigLength = bndOnBuild.getRefDict().getContigIDToLength().get(bndOnBuild.getChr());
            if (bndOnBuild.getBeginPos() < 0 || bndOnBuild.getEndPos() > contigLength) {
                // transcript region goes beyond chromosome, this is illegal
                LOGGER.warn("Skipping {}, transcript(s) goes beyond chromosome: {}", symbol, bndOnBuild);
                return Optional.empty();
            }
            // we can get negative position if we go way too upstream
            int paddingUpstream = Math.min(SquirlsDataBuilder.GENE_SEQUENCE_PADDING, bndOnBuild.getBeginPos());
            // we can go beyond chromosome if we go way too downstream
            int paddingDownstream = Math.min(contigLength - bndOnBuild.getEndPos(), SquirlsDataBuilder.GENE_SEQUENCE_PADDING);
            final GenomeInterval interval = bndOnBuild.withMorePadding(paddingUpstream, paddingDownstream);

            // Sequence
            final Optional<SequenceInterval> opt = genomeSequenceAccessor.fetchSequence(interval);
            if (opt.isEmpty()) {
                // no need to log, the complaint is made in the `fetchSequence` method
                return Optional.empty();
            }
            final String sequence = opt.get().getSequence();

            // PhyloP
            final float[] phylopScores = phylopAccessor.getScores(interval);

            // Calculate information content for transcripts
            final List<SplicingTranscript> stxs = txs.stream()
                    .map(calculator::calculate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());

            return Optional.of(new GeneAnnotationData(symbol, stxs, interval, sequence, phylopScores));
        };

    }

    /**
     * @param rd reference dictionary
     * @return predicate that returns <code>true</code> if the transcript model is located on contig recognized by <code>rd</code>
     */
    private static Predicate<? super TranscriptModel> transcriptsOnUnknownContigs(ReferenceDictionary rd) {
        return tm -> {
            final String contigName = tm.getTXRegion().getRefDict().getContigIDToName().get(tm.getChr());
            return rd.getContigNameToID().containsKey(contigName);
        };
    }

    private static Predicate<? super TranscriptModel> transcriptsOnNonPrimaryContigs() {
        return tm -> {
            String contigName = tm.getTXRegion().getRefDict().getContigIDToName().get(tm.getChr());
            contigName = contigName.startsWith("chr") ? contigName.substring(3) : contigName; // strip `chr`
            if (contigName.equals("X") || contigName.equals("Y") || contigName.equals("MT") || contigName.equals("M")) {
                // keep X, Y, M or MT
                return true;
            }
            try {
                final int chrNumber = Integer.parseInt(contigName);
                return 0 < chrNumber && chrNumber < 23;
            } catch (NumberFormatException e) {
                // swallow, this happens n case of 17_KI270729v1_random, 14_GL000225v1_random, etc.
            }
            return false;
        };
    }

    public void run(Collection<TranscriptModel> transcripts) {
        // group transcripts by gene symbol
        final Map<String, List<TranscriptModel>> txByGeneSymbol = transcripts.stream()
                .collect(groupingBy(TranscriptModel::getGeneSymbol));
        LOGGER.info("Processing {} genes", txByGeneSymbol.size());

        /*
        !WARNING! - the reference dictionary used by transcripts is not the same as the `rd` variable!
         */

        ProgressLogger progress = new ProgressLogger(500);
        int updated = txByGeneSymbol.entrySet().stream()
                .peek(e -> progress.logTotal("Processed {} genes"))
                .map(toGeneAnnotationData(genomeSequenceAccessor, phylopAccessor, calculator, rd))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(dao::insertGene)
                .reduce(Integer::sum)
                .orElse(0);

        LOGGER.info("Updated {} rows", updated);
    }
}
