package org.monarchinitiative.squirls.core.classifier.io;

import org.monarchinitiative.squirls.core.classifier.*;
import org.monarchinitiative.squirls.core.classifier.forest.RandomForest;
import org.monarchinitiative.squirls.core.classifier.transform.feature.FeatureTransformer;
import org.monarchinitiative.squirls.core.classifier.transform.feature.SplicingDataImputer;
import org.monarchinitiative.squirls.core.classifier.tree.BinaryDecisionTree;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This static utility class deserializes {@link SquirlsClassifier} from a YAML file. The class also provides raw
 * classifier data in form of {@link OverallModelData}.
 */
public class Deserializer {

    private static final AtomicInteger DONOR_TREE_COUNTER = new AtomicInteger();

    private static final AtomicInteger ACCEPTOR_TREE_COUNTER = new AtomicInteger();

    private Deserializer() {
        // private no-op
    }

    /**
     * Construct {@link SquirlsClassifier} from the YAML content that is read from the input stream.
     *
     * @param is input stream with YAML content
     * @return classifier
     */
    public static SquirlsClassifier deserialize(InputStream is) {
        return deserialize(deserializeOverallModelData(is));
    }

    public static SquirlsClassifier deserialize(OverallModelData data) {
        return StandardSquirlsClassifier.of(
                ThresholdingBinaryClassifier.of(deserializeDonorPipeline(data.getDonorClf()), data.getDonorThreshold()),
                ThresholdingBinaryClassifier.of(deserializeAcceptorPipeline(data.getAcceptorClf()), data.getAcceptorThreshold())
        );
    }

    /**
     * Deserialize YAML content from the input stream to {@link OverallModelData} format.
     *
     * @param is input stream
     * @return deserialized data
     */
    public static OverallModelData deserializeOverallModelData(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(OverallModelDataV041.class));
        return yaml.load(is);
    }

    static <T extends Classifiable> BinaryClassifier<T> deserializeDonorPipeline(PipelineTransferModel ptm) {
        return Pipeline.<T>builder()
                .name("donor")
                .classes(ptm.getRf().getClasses())
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeDonorClassifier(ptm))
                .build();
    }

    private static <T extends Classifiable> FeatureTransformer<T> deserializeImputer(List<String> featureNames, List<Double> featureStatistics) {
        return new SplicingDataImputer<>(featureNames, featureStatistics);
    }

    public static <T extends Classifiable> RandomForest<T> deserializeDonorClassifier(PipelineTransferModel ptm) {
        return RandomForest.<T>builder()
                .name("donor_rf")
                .classes(ptm.getRf().getClasses())
                .addTrees(ptm.getRf().getTrees().values().stream()
                        .map(Deserializer.<T>toDonorClassifierTree(ptm))
                        .collect(Collectors.toList()))
                .build();
    }

    public static <T extends Classifiable> Function<DecisionTreeTransferModel, BinaryDecisionTree<T>> toDonorClassifierTree(PipelineTransferModel ptm) {
        return md -> BinaryDecisionTree.<T>builder()
                .name(String.format("donor_tree_%d", DONOR_TREE_COUNTER.getAndIncrement()))
                .putAllFeatureIndices(ptm.getFeatureIndices())
                .classes(ptm.getRf().getClasses())
                .nNodes(md.getNodeCount())
                .features(md.getFeature())
                .childrenLeft(md.getChildrenLeft())
                .childrenRight(md.getChildrenRight())
                .thresholds(md.getThreshold())
                .values(md.getValues())
                .build();
    }

    static <T extends Classifiable> BinaryClassifier<T> deserializeAcceptorPipeline(PipelineTransferModel ptm) {
        return Pipeline.<T>builder()
                .name("acceptor")
                .classes(ptm.getRf().getClasses())
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeAcceptorClassifier(ptm))
                .build();
    }

    public static <T extends Classifiable> RandomForest<T> deserializeAcceptorClassifier(PipelineTransferModel ptm) {
        return RandomForest.<T>builder()
                .name("acceptor_rf")
                .classes(ptm.getRf().getClasses())
                .addTrees(ptm.getRf().getTrees().values().stream()
                        .map(Deserializer.<T>toAcceptorClassifierTree(ptm))
                        .collect(Collectors.toList()))
                .build();
    }

    public static <T extends Classifiable> Function<DecisionTreeTransferModel, BinaryDecisionTree<T>> toAcceptorClassifierTree(PipelineTransferModel ptm) {
        return md -> BinaryDecisionTree.<T>builder()
                .name(String.format("acceptor_tree_%d", ACCEPTOR_TREE_COUNTER.getAndIncrement()))
                .putAllFeatureIndices(ptm.getFeatureIndices())
                .classes(ptm.getRf().getClasses())
                .nNodes(md.getNodeCount())
                .features(md.getFeature())
                .childrenLeft(md.getChildrenLeft())
                .childrenRight(md.getChildrenRight())
                .thresholds(md.getThreshold())
                .values(md.getValues())
                .build();
    }

}
