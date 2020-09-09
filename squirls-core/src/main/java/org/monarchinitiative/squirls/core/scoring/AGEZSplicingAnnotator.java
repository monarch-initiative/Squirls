package org.monarchinitiative.squirls.core.scoring;

import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.ExclusionZoneFeatureCalculator;
import org.monarchinitiative.squirls.core.scoring.calculators.FeatureCalculator;
import org.monarchinitiative.squirls.core.scoring.calculators.PptIsTruncated;
import org.monarchinitiative.squirls.core.scoring.calculators.PyrimidineToPurineAtMinusThree;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This {@link SplicingAnnotator} implementation is used for evaluation of <em>AGEZ</em> features based on
 * <a href="https://pubmed.ncbi.nlm.nih.gov/32126153/">this paper</a>.
 * <p>
 * In addition to
 */
public class AGEZSplicingAnnotator extends AbstractSplicingAnnotator {

    public AGEZSplicingAnnotator(SplicingPwmData splicingPwmData,
                                 Map<String, Double> hexamerMap,
                                 Map<String, Double> septamerMap,
                                 BigWigAccessor bigWigAccessor) {
        super(new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters()),
                makeCalculatorMap(splicingPwmData, hexamerMap, septamerMap, bigWigAccessor));
    }

    static Map<String, FeatureCalculator> makeCalculatorMap(SplicingPwmData splicingPwmData,
                                                            Map<String, Double> hexamerMap,
                                                            Map<String, Double> septamerMap,
                                                            BigWigAccessor bigWigAccessor) {

        SplicingTranscriptLocator locator = new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters());
        AlleleGenerator generator = new AlleleGenerator(splicingPwmData.getParameters());

        // TODO - consider externalizing the AGEZ region definitions
        final Map<String, FeatureCalculator> agezCalculators = Map.of(
                "creates_ag_in_agez", new ExclusionZoneFeatureCalculator(locator),
                "ppt_is_truncated", new PptIsTruncated(locator),
                "yag_at_acceptor_minus_three", new PyrimidineToPurineAtMinusThree(locator, generator)
        );

        final Map<String, FeatureCalculator> denseCalculators = DenseSplicingAnnotator.makeDenseCalculatorMap(splicingPwmData, hexamerMap, septamerMap, bigWigAccessor);

        return Stream.concat(denseCalculators.entrySet().stream(), agezCalculators.entrySet().stream())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
