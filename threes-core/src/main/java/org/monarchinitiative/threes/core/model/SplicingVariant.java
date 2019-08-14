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

    /**
     * @return 1-based inclusive begin position of the variant
     */
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplicingVariant)) return false;

        SplicingVariant that = (SplicingVariant) o;

        if (coordinates != null ? !coordinates.equals(that.coordinates) : that.coordinates != null) return false;
        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
        return alt != null ? alt.equals(that.alt) : that.alt == null;

    }

    @Override
    public int hashCode() {
        int result = coordinates != null ? coordinates.hashCode() : 0;
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        result = 31 * result + (alt != null ? alt.hashCode() : 0);
        return result;
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
