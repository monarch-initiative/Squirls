package org.monarchinitiative.sss.core.scoring.scorers;

import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingIntron;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;
import org.monarchinitiative.sss.core.reference.allele.AlleleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class CrypticDonorForVariantsInDonorSite implements SplicingScorer<SplicingIntron> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrypticDonorForVariantsInDonorSite.class);

    private final SplicingInformationContentAnnotator icAnnotator;

    private final AlleleGenerator generator;

    private final int donorLength;

    public CrypticDonorForVariantsInDonorSite(SplicingInformationContentAnnotator icAnnotator, AlleleGenerator generator) {
        this.icAnnotator = icAnnotator;
        this.generator = generator;
        this.donorLength = icAnnotator.getSplicingParameters().getDonorLength();
    }

    @Override
    public double score(SplicingVariant variant, SplicingIntron region, SequenceInterval sequenceInterval) {

        // score SNPs that are located in the splice donor site and evaluate if they are likely to introduce new 5' CSS

        // this only scores SNPs at the moment
        if (variant.getRef().length() != 1 || variant.getAlt().length() != 1) {
            return Double.NaN;
        }

        final GenomeCoordinates varCoor = variant.getCoordinates();
        final AlleleGenerator.Region donor = generator.makeDonorRegion(region);
        if (!donor.overlapsWith(varCoor.getBegin(), varCoor.getEnd())) {
            // we only score SNPs that are located in the splice donor site
            return Double.NaN;
        }

        double refConsensusDonorScore = region.getDonorScore();

        String wtConsensusDonorSnippet = sequenceInterval.getSubsequence(varCoor.getBegin() - donorLength + 1, varCoor.getBegin())
                + variant.getRef()
                + sequenceInterval.getSubsequence(varCoor.getEnd(), varCoor.getEnd() + donorLength - 1);

        List<String> refWindows = SplicingScorer.slidingWindow(wtConsensusDonorSnippet, donorLength).collect(Collectors.toList());
        List<Double> refScores = new ArrayList<>(refWindows.size());

        for (String window : refWindows) {
            refScores.add(icAnnotator.getSpliceDonorScore(window));
        }

        ArgMaxIndexer<Double> refComparator = ArgMaxIndexer.makeIndexerFor(refScores, Comparator.reverseOrder());
        List<Integer> refIndices = refComparator.makeIndexList();
        refIndices.sort(refComparator);

        // -------------------------------------------------------------

        String altCrypticDonorSnippet = sequenceInterval.getSubsequence(varCoor.getBegin() - donorLength + 1, varCoor.getBegin())
                + variant.getAlt()
                + sequenceInterval.getSubsequence(varCoor.getEnd(), varCoor.getEnd() + donorLength - 1);


        List<String> altWindows = SplicingScorer.slidingWindow(altCrypticDonorSnippet, donorLength).collect(Collectors.toList());
        List<Double> altScores = new ArrayList<>(altWindows.size());

        for (String window : altWindows) {
            altScores.add(icAnnotator.getSpliceDonorScore(window));
        }

        ArgMaxIndexer<Double> altComparator = ArgMaxIndexer.makeIndexerFor(altScores, Comparator.reverseOrder());
        List<Integer> altIndices = altComparator.makeIndexList();
        altIndices.sort(altComparator);

        // -------------------------------------------------------------
        // the position of consensus splice site within sliding window
        int indexOfRefConsensus = refScores.indexOf(refConsensusDonorScore);
        if (indexOfRefConsensus != refIndices.get(0)) {
            // donor site does not have the largest IC score. Bizarre situation, but might happen
            LOGGER.debug("****\n\nDonor site does not have the largest IC score\n\n{}\n\n****", variant);
        }

        /*
        Get the second largest score in ref snippet. If there is a second site with high score, then the exon
        might be well defined by other means than just by canonical splice sites. In that case, we do not want
        to assign a high pathogenicity score for such an exon.

        However, if the second largest ref IC=-2 and variant presence creates a new site with IC=5, we might want
        to score the site higher. Even more, if the canonical donor site is a weaker one.
         */

        int crypticSiteIdx;
        if (indexOfRefConsensus == altIndices.get(0)) {
            // if the consensus site is still the strongest donor candidate from all positions in sliding window
            // then, the candidate cryptic site idx is the position with the second highest score
            crypticSiteIdx = altIndices.get(1);
        } else {
            // if consensus site is not the strongest donor candidate anymore
            // then, the candidate cryptic site idx is the position with the highest score within the window
            crypticSiteIdx = altIndices.get(0);
        }

        // get score of the position that results from alt allele presence
        final String donorSiteWithAltAllele = generator.getDonorSiteWithAltAllele(region.getBegin(), variant, sequenceInterval);
        if (donorSiteWithAltAllele == null) {
            // e.g. when the whole site is deleted. Other parts of analysis pipeline should interpret such events
            return Double.NaN;
        }

        double altConsensusDonorScore = icAnnotator.getSpliceDonorScore(donorSiteWithAltAllele);

        Double refCrypticScore = refScores.get(crypticSiteIdx);
        Double altCrypticScore = altScores.get(crypticSiteIdx);


        return -altConsensusDonorScore
                + altCrypticScore - refCrypticScore;
    }
}
