package org.monarchinitiative.squirls.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class UtilsTest {

    @ParameterizedTest
    @CsvSource({
            "2,1,2,3",
            "0,3,2,1",
            "1,1,3,2",
            "0,1,1,1"
    })
    void testArgmaxWithComparator(int expected, int one, int two, int three) {
        List<Integer> vals = List.of(one, two, three);
        final int argmax = Utils.argmax(vals, Integer::compare);
        assertThat(argmax, is(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,1,2,3",
            "0,3,2,1",
            "1,1,3,2",
            "0,1,1,1"
    })
    void testArgmaxWithComparable(int expected, int one, int two, int three) {
        List<Integer> vals = List.of(one, two, three);
        final int argmax = Utils.argmax(vals);
        assertThat(argmax, is(expected));
    }
}