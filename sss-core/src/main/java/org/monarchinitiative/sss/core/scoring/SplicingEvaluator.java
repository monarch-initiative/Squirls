package org.monarchinitiative.sss.core.scoring;

import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;

/**
 *
 */
public interface SplicingEvaluator {

    SplicingPathogenicityData evaluate(SplicingVariant variant, SplicingTranscript transcript);


//    default List<SplicingPathogenicityData> evaluateAll(List<SplicingVariant> variants) {
//        return variants.stream()
//                .map(this::evaluate)
//                .collect(Collectors.toList());
//    }
}
