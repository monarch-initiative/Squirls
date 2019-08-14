package org.monarchinitiative.threes.core;

import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 */
public class Utils {

    /**
     * Create subsequences/windows of size <code>'ws'</code> from nucleotide <code>sequence</code>.
     *
     * @param sequence {@link String} with nucleotide sequence to generate subsequences from
     * @param ws       window size
     * @return {@link Stream} of {@link String}s - subsequences of given <code>sequence</code> with length
     * <code>ws</code> or empty {@link Stream}, if '<code>ws</code> > <code>sequence.length()</code>'
     */
    public static Stream<String> slidingWindow(String sequence, int ws) {
        return ws > sequence.length()
                ? Stream.empty()
                : IntStream.range(0, sequence.length() - ws + 1)
                .boxed()
                .map(idx -> sequence.substring(idx, idx + ws));
    }



    /**
     * Apply <a href="https://en.wikipedia.org/wiki/Sigmoid_function">sigmoid</a> function to input variable
     * <code>x</code>.
     * <p>
     * Use default parameters:
     * <ul>
     * <li><b>steepness = -1</b></li>
     * <li><b>threshold = 0</b></li>
     * </ul>
     *
     * @param x input value
     * @return output in range (0, 1)
     */
    public static double sigmoid(double x) {
        return sigmoid(x, -1, 0);
    }


    /**
     * Apply <a href="https://en.wikipedia.org/wiki/Sigmoid_function">sigmoid</a> function to input variable
     * <code>x</code>.
     * <p>
     * Use parameters:
     *
     * @param x <b>value</b> value to be transformed
     * @param s <b>steepness</b> parameter - make sigmoid function more <em>threshold-like</em>
     * @param t <b>threshold</b> parameter - center the function on the <em>threshold</em> value
     * @return score
     */
    public static double sigmoid(double x, double s, double t) {
        return 1 / (1 + Math.exp(s * (x - t)));
    }

    public static UnaryOperator<Double> sigmoidScaler(double threshold, double steepness) {
        return d -> sigmoid(d, steepness, threshold);
    }
}
