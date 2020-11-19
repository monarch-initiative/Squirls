package org.monarchinitiative.squirls.cli.visualization.panel;

import de.charite.compbio.jannovar.annotation.Annotation;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator that sorts {@link Annotation} by transcript accession IDs to put transcripts with the lowest
 * <em>numbers</em> on top. Here, the <em>number</em> is e.g. <code>1234</code> for <em>NM_1234.5</em>, etc.
 */
class AnnotationComparator implements Comparator<Annotation> {

    private static final Pattern REFSEQ_PATTERN = Pattern.compile("^(?<prefix>(NM|NR|XM|XR))_(?<payload>\\d+)\\.(?<version>\\d+)$");
    private static final Pattern ENSEMBL_PATTERN = Pattern.compile("^(?<prefix>ENST)(?<payload>\\d{11})\\.(?<version>\\d+)$");
    private static final Pattern UCSC_PATTERN = Pattern.compile("^(?<prefix>uc)(?<payload>[\\d\\w]+)\\.(?<version>\\d+)$");
    private static final Pattern MITO_PATTERN = Pattern.compile("^(?<payload>MT\\w+|TRNF)$");


    @Override
    public int compare(Annotation leftAnn, Annotation rightAnn) {
        String leftAccession = leftAnn.getTranscript().getAccession();
        String rightAccession = rightAnn.getTranscript().getAccession();
        Matcher left, right;
        for (Pattern pattern : List.of(REFSEQ_PATTERN, ENSEMBL_PATTERN, UCSC_PATTERN, MITO_PATTERN)) {
            int result = 0;

            left = pattern.matcher(leftAccession);
            right = pattern.matcher(rightAccession);
            if (left.matches() && right.matches()) {
                for (String group : List.of("prefix", "payload", "version")) {
                    String l = left.group(group);
                    String r = right.group(group);
                    if (l != null && r != null) {
                        try {
                            int lint = Integer.parseInt(l);
                            int rint = Integer.parseInt(r);
                            result = Integer.compare(lint, rint);
                        } catch (NumberFormatException e) {
                            result = l.compareTo(r);
                        }
                        if (result != 0) {
                            return result;
                        }
                    }
                }

                return result;
            }
        }

        // do string sorting if the pattern matching failed

        return leftAccession.compareTo(rightAccession);
    }
}
