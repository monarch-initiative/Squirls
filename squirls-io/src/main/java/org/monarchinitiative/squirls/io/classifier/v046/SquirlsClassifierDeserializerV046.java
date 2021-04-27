package org.monarchinitiative.squirls.io.classifier.v046;

import org.monarchinitiative.squirls.core.classifier.*;
import org.monarchinitiative.squirls.io.SquirlsClassifierDeserializer;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;
import org.monarchinitiative.squirls.io.SquirlsSerializationException;
import org.monarchinitiative.squirls.io.classifier.Constants;
import org.monarchinitiative.squirls.io.classifier.Pipeline;
import org.monarchinitiative.squirls.io.classifier.SplicingDataImputer;
import org.monarchinitiative.squirls.io.classifier.reference.DecisionTreeTransferModel;
import org.monarchinitiative.squirls.io.classifier.reference.PipelineTransferModel;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Daniel Danis
 */
public class SquirlsClassifierDeserializerV046 implements SquirlsClassifierDeserializer {

    private static final AtomicInteger DONOR_TREE_COUNTER = new AtomicInteger();

    private static final AtomicInteger ACCEPTOR_TREE_COUNTER = new AtomicInteger();

    private final Yaml yaml;

    public SquirlsClassifierDeserializerV046() {
        yaml = new Yaml(new Constructor(OverallModelDataV046.class));
    }

    private static <T extends SquirlsFeatures> BinaryClassifier<T> deserializeDonorPipeline(PipelineTransferModel ptm) {
        return Pipeline.<T>builder()
                .name(Constants.DONOR_PIPE_NAME)
                .classes(ptm.getRf().getClasses())
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeDonorClassifier(ptm))
                .build();
    }

    private static <T extends SquirlsFeatures> RandomForest<T> deserializeDonorClassifier(PipelineTransferModel ptm) {
        return RandomForest.<T>builder()
                .name(Constants.DONOR_RF_NAME)
                .classes(ptm.getRf().getClasses())
                .addTrees(ptm.getRf().getTrees().values().stream()
                        .map(SquirlsClassifierDeserializerV046.<T>toDonorClassifierTree(ptm))
                        .collect(Collectors.toList()))
                .build();
    }

    private static <T extends SquirlsFeatures> Function<DecisionTreeTransferModel, BinaryDecisionTree<T>> toDonorClassifierTree(PipelineTransferModel ptm) {
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


    private static <T extends SquirlsFeatures> BinaryClassifier<T> deserializeAcceptorPipeline(PipelineTransferModel ptm) {
        return Pipeline.<T>builder()
                .name(Constants.ACCEPTOR_PIPE_NAME)
                .classes(ptm.getRf().getClasses())
                .transformer(deserializeImputer(ptm.getFeatureNames(), ptm.getFeatureStatistics()))
                .classifier(deserializeAcceptorClassifier(ptm))
                .build();
    }

    private static <T extends SquirlsFeatures> RandomForest<T> deserializeAcceptorClassifier(PipelineTransferModel ptm) {
        return RandomForest.<T>builder()
                .name(Constants.ACCEPTOR_RF_NAME)
                .classes(ptm.getRf().getClasses())
                .addTrees(ptm.getRf().getTrees().values().stream()
                        .map(SquirlsClassifierDeserializerV046.<T>toAcceptorClassifierTree(ptm))
                        .collect(Collectors.toList()))
                .build();
    }

    private static <T extends SquirlsFeatures> Function<DecisionTreeTransferModel, BinaryDecisionTree<T>> toAcceptorClassifierTree(PipelineTransferModel ptm) {
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

    private static <T extends SquirlsFeatures> SplicingDataImputer<T> deserializeImputer(List<String> featureNames, List<Double> featureStatistics) {
        return new SplicingDataImputer<>(featureNames, featureStatistics);
    }

    @Override
    public Set<SquirlsClassifierVersion> supportedVersions() {
        return Set.of(SquirlsClassifierVersion.v0_4_6);
    }

    @Override
    public SquirlsClassifier deserialize(InputStream is) throws SquirlsSerializationException {
        OverallModelDataV046 data = yaml.load(is);
        ScalingTransferModelV046 scaling = data.getScaling();
        // coefficients
        if (scaling.getLogisticRegressionCoef().size() != 1) {
            throw new SquirlsSerializationException("Unexpected number of coefficient lists (" + scaling.getLogisticRegressionCoef().size()
                    + ") that is not equal to 1");
        }
        List<Double> coefficients = scaling.getLogisticRegressionCoef().get(0);

        // intercept
        int interceptCount = scaling.getLogisticRegressionIntercept().size();
        if (interceptCount != 1) {
            throw new SquirlsSerializationException("Unexpected number of intercept values (" + interceptCount
                    + ") that is not equal to 1");
        }
        double intercept = scaling.getLogisticRegressionIntercept().get(0);

        // threshold
        if (data.getThreshold().size() != 1) {
            throw new SquirlsSerializationException("Unexpected number of threshold values (" + data.getThreshold().size()
                    + ") that is not equal to 1");
        }
        double threshold = data.getThreshold().get(0);

        return new SquirlsClassifierV046(
                deserializeDonorPipeline(data.getDonorClf()),
                deserializeAcceptorPipeline(data.getAcceptorClf()),
                scaling.getStandardScalerMean(), scaling.getStandardScalerVar(), coefficients, intercept,
                threshold);
    }

}
