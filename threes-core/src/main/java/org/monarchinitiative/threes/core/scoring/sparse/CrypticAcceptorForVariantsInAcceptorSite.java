package org.monarchinitiative.threes.core.scoring.sparse;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.Utils;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.model.SplicingIntron;
import org.monarchinitiative.threes.core.model.SplicingRegion;
import org.monarchinitiative.threes.core.model.SplicingTernate;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class CrypticAcceptorForVariantsInAcceptorSite implements SplicingScorer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrypticDonorForVariantsInDonorSite.class);

    private final SplicingInformationContentCalculator icAnnotator;

    private final AlleleGenerator generator;

    private final int acceptorLength;

    public CrypticAcceptorForVariantsInAcceptorSite(SplicingInformationContentCalculator icAnnotator, AlleleGenerator generator) {
        this.icAnnotator = icAnnotator;
        this.generator = generator;
        this.acceptorLength = icAnnotator.getSplicingParameters().getAcceptorLength();
    }


    @Override
    public Function<SplicingTernate, Double> scoringFunction() {
        return t -> {
            final SplicingRegion region = t.getRegion();
            final SequenceInterval sequenceInterval = t.getSequenceInterval();
            final GenomeVariant variant = t.getVariant();

            if (!(region instanceof SplicingIntron)) {
                return Double.NaN;
            }
            final SplicingIntron intron = (SplicingIntron) region;
            // score SNPs that are located in the splice acceptor site and evaluate if they are likely to introduce new 3' CSS

            // this only scores SNPs at the moment
            if (variant.getRef().length() != 1 || variant.getAlt().length() != 1) {
                return Double.NaN;
            }

            final GenomeInterval variantInterval = variant.getGenomeInterval();
            final GenomeInterval acceptor = icAnnotator.getSplicingParameters().makeAcceptorRegion(intron.getInterval().getGenomeEndPos());
            if (!acceptor.overlapsWith(variantInterval)) {
                // we only score SNPs that are located in the splice acceptor site
                return Double.NaN;
            }

            final GenomeInterval upstream = new GenomeInterval(variantInterval.getGenomeBeginPos().shifted(-acceptorLength + 1), acceptorLength - 1);
            final GenomeInterval downstream = new GenomeInterval(variantInterval.getGenomeEndPos(), acceptorLength - 1);
            final Optional<String> upsSeq = sequenceInterval.getSubsequence(upstream);
            final Optional<String> downSeq = sequenceInterval.getSubsequence(downstream);
            if (upsSeq.isEmpty() || downSeq.isEmpty()) {
                LOGGER.info("Not enough of fasta sequence provided for variant `{}` - sequence: `{}`, required: `{}`/`{}`",
                        variant, sequenceInterval.getInterval(), upstream, downstream);
                return Double.NaN;
            }

            String wtConsensusAcceptorSnippet = upsSeq.get() + variant.getRef() + downSeq.get();
            List<String> refWindows = Utils.slidingWindow(wtConsensusAcceptorSnippet, acceptorLength).collect(Collectors.toList());
            List<Double> refScores = new ArrayList<>(refWindows.size());

            for (String window : refWindows) {
                refScores.add(icAnnotator.getSpliceAcceptorScore(window));
            }

            ArgMaxIndexer<Double> refComparator = ArgMaxIndexer.makeIndexerFor(refScores, Comparator.reverseOrder());
            List<Integer> refIndices = refComparator.makeIndexList();
            refIndices.sort(refComparator);

            // -------------------------------------------------------------

            String altCrypticAcceptorSnippet = upsSeq.get()
                    + variant.getAlt()
                    + downSeq.get();
            List<String> altWindows = Utils.slidingWindow(altCrypticAcceptorSnippet, acceptorLength).collect(Collectors.toList());
            List<Double> altScores = new ArrayList<>(altWindows.size());

            for (String window : altWindows) {
                altScores.add(icAnnotator.getSpliceAcceptorScore(window));
            }

            ArgMaxIndexer<Double> altComparator = ArgMaxIndexer.makeIndexerFor(altScores, Comparator.reverseOrder());
            List<Integer> altIndices = altComparator.makeIndexList();
            altIndices.sort(altComparator);

            // -------------------------------------------------------------
            // the position of consensus splice site within sliding window
            double refConsensusAcceptorScore = intron.getAcceptorScore();
            int indexOfRefConsensus = refScores.indexOf(refConsensusAcceptorScore);
            if (indexOfRefConsensus != refIndices.get(0)) {
                // acceptor site does not have the largest IC score. Bizarre situation, but might happen
                LOGGER.debug("****\nAcceptor site does not have the largest IC score\n\n{}\n", variant);
            }

            /*
            Get the second largest score in ref snippet. If there is a second site with high score, then the exon
            might be well defined by other means than just by canonical splice sites. In that case, we do not want
            to assign a high pathogenicity score for such an exon.

            However, if the second largest ref $R_i = -2$ and variant presence creates a new site with $R_i = 5$, then
            we want to score the site higher. Even more, if the canonical acceptor site is a weaker one.
            */


            int crypticSiteIdx;
            if (indexOfRefConsensus == altIndices.get(0)) {
                // if the consensus site is still the strongest acceptor candidate from all positions in sliding window
                // then, the cryptic site idx is the position with the second highest score
                crypticSiteIdx = altIndices.get(1);
            } else {
                // if consensus site is not the strongest acceptor candidate anymore
                // then, the cryptic site idx is the first position in the window
                crypticSiteIdx = altIndices.get(0);
            }

            // get score of the position that results from alt allele presence
            String acceptorSiteWithAltAllele = generator.getAcceptorSiteWithAltAllele(intron.getInterval().getGenomeEndPos(), variant, sequenceInterval);
            if (acceptorSiteWithAltAllele == null) {
                // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
                return Double.NaN;
            }

            double altConsensusAcceptorScore = icAnnotator.getSpliceAcceptorScore(acceptorSiteWithAltAllele);

            Double refCrypticScore = refScores.get(crypticSiteIdx);
            Double altCrypticScore = altScores.get(crypticSiteIdx);

            return -altConsensusAcceptorScore
                    + altCrypticScore - refCrypticScore; // consider the cryptic site, how much did it gain?
        };
    }
}