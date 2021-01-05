package org.monarchinitiative.squirls.io;

import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.SquirlsFeatures;
import org.monarchinitiative.squirls.io.classifier.v041.SquirlsClassifierDeserializerV041;
import org.monarchinitiative.squirls.io.classifier.v046.SquirlsClassifierDeserializerV046;

import java.io.InputStream;
import java.util.Set;

public interface SquirlsClassifierDeserializer {

    static <T extends SquirlsFeatures> SquirlsClassifierDeserializer forVersion(SquirlsClassifierVersion version) {
        switch (version) {
            case v0_4_1:
            case v0_4_4:
                return new SquirlsClassifierDeserializerV041();
            case v0_4_6:
                return new SquirlsClassifierDeserializerV046();
            default:
                throw new IllegalArgumentException("Unsupported version " + version);
        }
    }

    Set<SquirlsClassifierVersion> supportedVersions();

    SquirlsClassifier deserialize(InputStream is) throws SquirlsSerializationException;

}
