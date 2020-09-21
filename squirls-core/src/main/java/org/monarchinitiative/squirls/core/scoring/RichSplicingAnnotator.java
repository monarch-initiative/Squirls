package org.monarchinitiative.squirls.core.scoring;

import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.calculators.*;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This annotator calculates the following splicing features for each {@link Annotatable}:
 * <ul>
 *     <li><code>canonical_donor</code></li>
 *     <li><code>cryptic_donor</code></li>
 *     <li><code>canonical_acceptor</code></li>
 *     <li><code>cryptic_acceptor</code></li>
 *     <li><code>cryptic_acceptor</code></li>
 *     <li><code>hexamer</code></li>
 *     <li><code>septamer</code></li>
 *     <li><code>phylop</code></li>
 *     <li><code>donor_offset</code></li>
 *     <li><code>acceptor_offset</code></li>
 *     <li><code>wt_ri_donor</code></li>
 *     <li><code>wt_ri_acceptor</code></li>
 *     <li><code>alt_ri_best_window_donor</code></li>
 *     <li><code>alt_ri_best_window_acceptor</code></li>
 *     <li><code>s_strength_diff_donor</code></li>
 *     <li><code>s_strength_diff_acceptor</code></li>
 *     <li><code>exon_length</code></li>
 *     <li><code>intron_length</code></li>
 * </ul>
 * The {@link de.charite.compbio.jannovar.reference.GenomePosition}s of the closest donor and acceptor sites.
 * <p>
 * Note that the features are computed only if the transcript has at least a single intron.
 */
public class RichSplicingAnnotator extends AbstractSplicingAnnotator {

    public RichSplicingAnnotator(SplicingPwmData splicingPwmData,
                                 Map<String, Double> hexamerMap,
                                 Map<String, Double> septamerMap,
                                 BigWigAccessor bigWigAccessor) {
        super(new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters()),
                Stream.of(
                        // rich
                        makeCalculatorMap(splicingPwmData).entrySet(),
                        // dense
                        DenseSplicingAnnotator.makeDenseCalculatorMap(splicingPwmData, hexamerMap, septamerMap, bigWigAccessor).entrySet())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    static Map<String, FeatureCalculator> makeCalculatorMap(SplicingPwmData splicingPwmData) {

        SplicingTranscriptLocator locator = new NaiveSplicingTranscriptLocator(splicingPwmData.getParameters());
        SplicingInformationContentCalculator calculator = new SplicingInformationContentCalculator(splicingPwmData);
        AlleleGenerator generator = new AlleleGenerator(splicingPwmData.getParameters());

        return Map.of(
                "wt_ri_donor", new WtRiDonor(calculator, generator, locator),
                "wt_ri_acceptor", new WtRiAcceptor(calculator, generator, locator),
                "alt_ri_best_window_donor", new BestWindowAltRiCrypticDonor(calculator, generator),
                "alt_ri_best_window_acceptor", new BestWindowAltRiCrypticAcceptor(calculator, generator),
                "s_strength_diff_donor", new SStrengthDiffDonor(calculator, generator, locator),
                "s_strength_diff_acceptor", new SStrengthDiffAcceptor(calculator, generator, locator),
                "exon_length", new ExonLength(locator),
                "intron_length", new IntronLength(locator));
    }

}
