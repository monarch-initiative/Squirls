package org.monarchinitiative.squirls.core.classifier.io;

import org.monarchinitiative.squirls.core.classifier.*;
import org.monarchinitiative.squirls.core.classifier.forest.RandomForest;
import org.monarchinitiative.squirls.core.classifier.transform.feature.FeatureTransformer;
import org.monarchinitiative.squirls.core.classifier.transform.feature.SplicingDataImputer;
import org.monarchinitiative.squirls.core.classifier.tree.AcceptorSplicingDecisionTree;
import org.monarchinitiative.squirls.core.classifier.tree.DonorSplicingDecisionTree;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class deserializes {@link SquirlsClassifier} from a YAML file.
 */
public class Deserializer {

    private Deserializer() {
        // private no-op
    }

    public static SquirlsClassifier deserialize(InputStream is) {
        return deserialize(deserializeOverallModelData(is));
    }

    public static SquirlsClassifier deserialize(OverallModelData data) {
        return StandardSquirlsClassifier.builder()
                .donorClf(deserializeDonorPipeline(data.getDonorClf()))
                .donorThreshold(data.getDonorThreshold())
                .acceptorClf(deserializeAcceptorPipeline(data.getAcceptorClf()))
                .acceptorThreshold(data.getAcceptorThreshold())
                .build();
    }

    public static OverallModelData deserializeOverallModelData(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(OverallModelData.class));
        return yaml.load(is);
    }

    static <T extends Classifiable> BinaryClassifier<T> deserializeDonorPipeline(PipelineTransferModel ptm) {
        return Pipeline.<T>builder()
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeDonorClassifier(ptm.getRf()))
                .build();
    }

    private static <T extends Classifiable> FeatureTransformer<T> deserializeImputer(List<String> featureNames, List<Double> featureStatistics) {
        return new SplicingDataImputer<>(featureNames, featureStatistics);
    }

    public static <T extends Classifiable> RandomForest<T> deserializeDonorClassifier(RandomForestTransferModel rfModel) {
        return RandomForest.<T>builder()
                .classes(rfModel.getClasses())
                .addTrees(rfModel.getTrees().values().stream()
                        .map(Deserializer.<T>toDonorClassifierTree(rfModel.getClasses()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static <T extends Classifiable> Function<DecisionTreeTransferModel, DonorSplicingDecisionTree<T>> toDonorClassifierTree(List<Integer> classes) {
        return md -> DonorSplicingDecisionTree.<T>builder()
                .classes(classes)
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
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeAcceptorClassifier(ptm.getRf()))
                .build();
    }

    public static <T extends Classifiable> RandomForest<T> deserializeAcceptorClassifier(RandomForestTransferModel rfModel) {
        return RandomForest.<T>builder()
                .classes(rfModel.getClasses())
                .addTrees(rfModel.getTrees().values().stream()
                        .map(Deserializer.<T>toAcceptorClassifierTree(rfModel.getClasses()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static <T extends Classifiable> Function<DecisionTreeTransferModel, AcceptorSplicingDecisionTree<T>> toAcceptorClassifierTree(List<Integer> classes) {
        return md -> AcceptorSplicingDecisionTree.<T>builder()
                .classes(classes)
                .nNodes(md.getNodeCount())
                .features(md.getFeature())
                .childrenLeft(md.getChildrenLeft())
                .childrenRight(md.getChildrenRight())
                .thresholds(md.getThreshold())
                .values(md.getValues())
                .build();
    }

}
