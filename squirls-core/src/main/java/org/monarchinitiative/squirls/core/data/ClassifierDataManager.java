package org.monarchinitiative.squirls.core.data;

import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.transform.prediction.PredictionTransformer;

import java.util.Collection;
import java.util.Optional;

public interface ClassifierDataManager {

    /**
     * @return collection with versions of available classifiers
     */
    Collection<String> getAvailableClassifiers();

    /**
     * Store the classifier under particular version.
     */
    int storeClassifier(String version, byte[] clfBytes);

    /**
     * Read classifier data provided it exists in the underlying resource.
     *
     * @param version of classifier to read
     * @return classifier data or <code>null</code> of the particular version is not available
     */
    Optional<SquirlsClassifier> readClassifier(String version);

    /**
     * Store <code>transformer</code> under given <code>version</code>.
     */
    int storeTransformer(String version, PredictionTransformer transformer);

    /**
     * Read given transformer.
     *
     * @param version of transformer to read
     * @return transformer data
     */
    Optional<PredictionTransformer> readTransformer(String version);
}
