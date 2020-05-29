package org.monarchinitiative.threes.core.classifier.io;

import org.monarchinitiative.threes.core.classifier.*;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;
import org.monarchinitiative.threes.core.classifier.impute.SplicingDataImputer;
import org.monarchinitiative.threes.core.classifier.tree.AcceptorSplicingDecisionTree;
import org.monarchinitiative.threes.core.classifier.tree.DonorSplicingDecisionTree;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class deserializes {@link OverlordClassifier} from a YAML file.
 */
public class Deserializer {

    private Deserializer() {
        // private no-op
    }

    public static OverlordClassifier deserialize(InputStream is) {
        return deserialize(deserializeOverallModelData(is));
    }

    public static OverlordClassifier deserialize(OverallModelData data) {
        return StandardOverlordClassifier.builder()
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

    static BinaryClassifier<FeatureData> deserializeDonorPipeline(PipelineTransferModel ptm) {
        return Pipeline.builder()
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeDonorClassifier(ptm.getRf()))
                .build();
    }

    private static FeatureTransformer<FeatureData> deserializeImputer(List<String> featureNames, List<Double> featureStatistics) {
        return new SplicingDataImputer(featureNames, featureStatistics);
    }

    public static RandomForest<FeatureData> deserializeDonorClassifier(RandomForestTransferModel rfModel) {
        return RandomForest.builder()
                .classes(rfModel.getClasses())
                .addTrees(rfModel.getTrees().values().stream()
                        .map(toDonorClassifierTree(rfModel.getClasses()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static Function<DecisionTreeTransferModel, DonorSplicingDecisionTree> toDonorClassifierTree(List<Integer> classes) {
        return md -> DonorSplicingDecisionTree.builder()
                .classes(classes)
                .nNodes(md.getNodeCount())
                .features(md.getFeature())
                .childrenLeft(md.getChildrenLeft())
                .childrenRight(md.getChildrenRight())
                .thresholds(md.getThreshold())
                .values(md.getValues())
                .build();
    }

    static BinaryClassifier<FeatureData> deserializeAcceptorPipeline(PipelineTransferModel ptm) {
        return Pipeline.builder()
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeAcceptorClassifier(ptm.getRf()))
                .build();
    }

    public static RandomForest<FeatureData> deserializeAcceptorClassifier(RandomForestTransferModel rfModel) {
        return RandomForest.builder()
                .classes(rfModel.getClasses())
                .addTrees(rfModel.getTrees().values().stream()
                        .map(toAcceptorClassifierTree(rfModel.getClasses()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static Function<DecisionTreeTransferModel, AcceptorSplicingDecisionTree> toAcceptorClassifierTree(List<Integer> classes) {
        return md -> AcceptorSplicingDecisionTree.builder()
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
