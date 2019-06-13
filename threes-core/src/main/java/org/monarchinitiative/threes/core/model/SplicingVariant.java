package org.monarchinitiative.threes.core.model;

/**
 *
 */
public class SplicingVariant {

    private final GenomeCoordinates coordinates;

    private final String ref;

    private final String alt;


    private SplicingVariant(Builder builder) {
        coordinates = builder.coordinates;
        ref = builder.ref;
        alt = builder.alt;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public String getContig() {
        return coordinates.getContig();
    }

    public int getPos() {
        return coordinates.getBegin() + 1;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public GenomeCoordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "spl v [" + getContig() + ":" + getPos() + ref + ">" + alt + "]";
    }

    public static final class Builder {

        private GenomeCoordinates coordinates;


        private String ref;

        private String alt;


        private Builder() {
        }

        public Builder setCoordinates(GenomeCoordinates coordinates) {
            this.coordinates = coordinates;
            return this;
        }

        public Builder setRef(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder setAlt(String alt) {
            this.alt = alt;
            return this;
        }


        public SplicingVariant build() {
            return new SplicingVariant(this);
        }
    }
}
