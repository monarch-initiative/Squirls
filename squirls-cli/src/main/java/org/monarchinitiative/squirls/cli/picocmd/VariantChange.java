package org.monarchinitiative.squirls.cli.picocmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VariantChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantChange.class);

    private static final Pattern VARIANT_PATTERN = Pattern.compile("(?<contig>(chr)?([\\d]{1,2}|X|Y|M])):(?<position>\\d+)(?<ref>[ACGTacgt]+)>(?<alt>[ACGTacgt]+)");
    private final String contig;
    private final int pos;
    private final String ref, alt;

    private VariantChange(String contig, int pos, String ref, String alt) {
        this.contig = contig;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    static Optional<VariantChange> fromString(String payload) {
        final Matcher matcher = VARIANT_PATTERN.matcher(payload);
        if (!matcher.matches()) {
            LOGGER.warn("Invalid variant data: `{}`", payload);
            return Optional.empty();
        }

        final String contig = matcher.group("contig");
        final String prefixedContig = contig.startsWith("chr") ? contig : "chr" + contig;
        final String position = matcher.group("position");
        final int pos;
        try {
            pos = Integer.parseInt(position);
            if (pos <= 0) {
                LOGGER.warn("Position must be positive integer: `{}`", payload);
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Position `{}` is not valid number", position);
            return Optional.empty();
        }
        return Optional.of(new VariantChange(prefixedContig, pos, matcher.group("ref"), matcher.group("alt")));
    }

    public String getContig() {
        return contig;
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public String getVariantChange() {
        return contig + ":" + pos + ref + ">" + alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantChange that = (VariantChange) o;
        return pos == that.pos &&
                Objects.equals(contig, that.contig) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(alt, that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, pos, ref, alt);
    }

    @Override
    public String toString() {
        return "VariantChange{" +
                "contig='" + contig + '\'' +
                ", pos=" + pos +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                '}';
    }
}
