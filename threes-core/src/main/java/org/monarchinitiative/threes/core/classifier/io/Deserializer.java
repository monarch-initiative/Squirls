package org.monarchinitiative.threes.core.classifier.io;

import org.monarchinitiative.threes.core.classifier.FeatureData;
import org.monarchinitiative.threes.core.classifier.OverlordClassifier;
import org.monarchinitiative.threes.core.classifier.SimpleOverlordClassifier;
import org.monarchinitiative.threes.core.classifier.forest.RandomForest;
import org.monarchinitiative.threes.core.classifier.tree.AcceptorSplicingDecisionTree;
import org.monarchinitiative.threes.core.classifier.tree.DonorSplicingDecisionTree;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Deserializer {

    public static OverlordClassifier deserialize(InputStream is) throws IOException {
        OverallModelData data = deserializeOverallModelData(is);

        return SimpleOverlordClassifier.builder()
                .donorClf(deserializeDonorClassifier(data.getDonorClf()))
                .donorThreshold(data.getDonorThreshold())
                .acceptorClf(deserializeAcceptorClassifier(data.getAcceptorClf()))
                .acceptorThreshold(data.getAcceptorThreshold())
                .build();
    }

    static OverallModelData deserializeOverallModelData(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(OverallModelData.class));
        return yaml.load(is);
    }

    static RandomForest<FeatureData> deserializeDonorClassifier(RandomForestTransferModel rfModel) {
        return RandomForest.<FeatureData>builder()
                .classes(rfModel.getClasses())
                .addTrees(rfModel.getTrees().values().stream()
                        .map(toDonorClassifierTree(rfModel.getClasses()))
                        .collect(Collectors.toList())
                )
                .build();
    }

    private static Function<DecisionTreeTransferModel, DonorSplicingDecisionTree> toDonorClassifierTree(List<Integer> classes) {
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

    private static RandomForest<FeatureData> deserializeAcceptorClassifier(RandomForestTransferModel rfModel) {
        return RandomForest.<FeatureData>builder()
                .classes(rfModel.getClasses())
                .addTrees(rfModel.getTrees().values().stream()
                        .map(toAcceptorClassifierTree(rfModel.getClasses()))
                        .collect(Collectors.toList())
                )
                .build();
    }

    private static Function<DecisionTreeTransferModel, AcceptorSplicingDecisionTree> toAcceptorClassifierTree(List<Integer> classes) {
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
