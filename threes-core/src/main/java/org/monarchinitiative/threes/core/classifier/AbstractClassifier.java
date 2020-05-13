package org.monarchinitiative.threes.core.classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractClassifier<T> implements Classifier<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClassifier.class);

    /**
     * Array with class labels, e.g. [0,1] for binary classification.
     */
    protected final int[] classes;


    protected AbstractClassifier(Builder<?> builder) {
        this.classes = toIntArray(builder.classes);
        check();
    }

    /**
     * Convert list of integers to integer array.
     *
     * @param integers list of integers
     * @return integer array
     */
    protected static int[] toIntArray(List<Integer> integers) {
        int[] array = new int[integers.size()];
        for (int i = 0; i < integers.size(); i++) {
            array[i] = integers.get(i);
        }
        return array;
    }

    /**
     * Convert list of doubles to double array.
     *
     * @param doubles list of doubles
     * @return double array
     */
    protected static double[] toDoubleArray(List<Double> doubles) {
        double[] array = new double[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            array[i] = doubles.get(i);
        }
        return array;
    }

    /**
     * Sanity checks for an abstract classifier.
     * <p>
     * We check that:
     * <ul>
     * <li>the classes attribute must not be empty, it must contain at least 2 values</li>
     * </ul>
     */
    private void check() {
        if (classes.length < 2) {
            LOGGER.warn("The `classes` attribute must contain at least 2 class labels");
            throw new RuntimeException(String.format("The `classes` attribute must contain at least 2 class labels. Found `%d`", classes.length));
        }
    }

    public abstract static class Builder<A extends Builder<A>> {

        private List<Integer> classes;

        public A classes(List<Integer> classes) {
            this.classes = List.copyOf(classes);
            return self();
        }

        protected abstract AbstractClassifier<?> build();

        protected abstract A self();
    }
}
