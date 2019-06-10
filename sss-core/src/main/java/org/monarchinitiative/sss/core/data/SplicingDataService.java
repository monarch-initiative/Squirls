package org.monarchinitiative.sss.core.data;

import org.monarchinitiative.sss.core.pwm.SplicingInformationContentAnnotator;

import java.util.Map;

/**
 *
 */
public interface SplicingDataService {

    SplicingTranscriptSource getSplicingTranscriptSource();

    SplicingInformationContentAnnotator getSplicingInformationContentAnnotator();

    Map<String, Integer> getContigLengthMap();

}
