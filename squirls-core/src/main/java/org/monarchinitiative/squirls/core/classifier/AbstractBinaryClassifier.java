package org.monarchinitiative.squirls.core.classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class AbstractBinaryClassifier<T extends Classifiable> implements BinaryClassifier<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBinaryClassifier.class);

    protected final String name;

    /**
     * Array with class labels, e.g. [0,1] for binary classification.
     */
    protected final int[] classes;


    protected AbstractBinaryClassifier(Builder<?> builder) {
        this.classes = toIntArray(builder.classes);
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
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

    @Override
    public String getName() {
        return name;
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
     * <li>the classes attribute must not be empty, it must contain 2 values (we only support binary classification here)</li>
     * </ul>
     */
    private void check() {
        if (classes.length != 2) {
            LOGGER.warn("The `classes` attribute must contain exactly 2 class labels");
            throw new RuntimeException(String.format("The `classes` attribute must contain exactly 2 class labels. Found `%d`", classes.length));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBinaryClassifier<?> that = (AbstractBinaryClassifier<?>) o;
        return Arrays.equals(classes, that.classes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(classes);
    }

    @Override
    public String toString() {
        return "AbstractBinaryClassifier{" +
                "classes=" + Arrays.toString(classes) +
                '}';
    }

    public abstract static class Builder<A extends Builder<A>> {

        private String name;

        private final List<Integer> classes = new ArrayList<>();

        protected Builder() {
            // protected no-op
        }

        public A name(String name) {
            this.name = name;
            return self();
        }

        public A classes(List<Integer> classes) {
            this.classes.addAll(classes);
            return self();
        }

        public A classes(Integer... classes) {
            this.classes.addAll(Arrays.asList(classes));
            return self();
        }

        protected abstract AbstractBinaryClassifier<?> build();

        protected abstract A self();
    }
}
