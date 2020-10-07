package org.monarchinitiative.squirls.core.scoring;

import java.util.HashMap;
import java.util.Map;

class Utils {

    /**
     * Map for translation of a single nucleotide/base symbol into its reverse complement base
     */
    private static final Map<Character, Character> IUPAC_COMPLEMENT_MAP;

    static {
        Map<Character, Character> TEMPORARY = new HashMap<>();
        TEMPORARY.putAll(
                Map.of(
                        // STANDARD
                        'A', 'T',
                        'C', 'G',
                        'G', 'C',
                        'T', 'A',
                        'U', 'A'));
        TEMPORARY.putAll(
                Map.of(
                        // AMBIGUITY BASES - first part
                        'W', 'W', // weak - A,T
                        'S', 'S', // strong - C,G
                        'M', 'K', // amino - A,C
                        'K', 'M', // keto - G,T
                        'R', 'Y', // purine - A,G
                        'Y', 'R')); // pyrimidine - C,T

        TEMPORARY.putAll(
                Map.of(
                        // AMBIGUITY BASES - second part
                        'B', 'V', // not A
                        'D', 'H', // not C
                        'H', 'D', // not G
                        'V', 'B', // not T
                        'N', 'N' // any one base
                )
        );

        IUPAC_COMPLEMENT_MAP = Map.copyOf(TEMPORARY);
    }


    /**
     * Convert nucleotide sequence to reverse complement. If <code>U</code> is present, it is complemented to
     * <code>A</code> as expected. However, <code>A</code> is always complemented to <code>T</code>.
     *
     * @param sequence of nucleotides in IUPAC notation
     * @return reverse complement of given <code>sequence</code>
     */
    static String reverseComplement(String sequence) {
        char[] oldSeq = sequence.toCharArray();
        char[] newSeq = new char[oldSeq.length];
        int idx = oldSeq.length - 1;
        for (int i = 0; i < oldSeq.length; i++) {
            char template = oldSeq[i];
            boolean isUpperCase = Character.isUpperCase(template);

            char toLookUp = isUpperCase
                    ? template // no-op, the IUPAC map contains upper-case characters
                    : Character.toUpperCase(template);

            char complement = IUPAC_COMPLEMENT_MAP.getOrDefault(toLookUp, template);
            char complementWithCase = isUpperCase
                    ? complement // no-op, the IUPAC map contains upper-case characters
                    : Character.toLowerCase(complement);

            newSeq[idx - i] = complementWithCase;
        }
        return new String(newSeq);
    }

}
