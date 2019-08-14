package org.monarchinitiative.threes.core.calculators.sms;

import java.util.Map;

public interface SMSParser {

    /**
     * @return Map with SMS scores for all 7-mers
     */
    Map<String, Double> getSeptamerMap();
}
