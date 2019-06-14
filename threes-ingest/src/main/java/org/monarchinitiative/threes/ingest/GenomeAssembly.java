package org.monarchinitiative.threes.ingest;

public enum GenomeAssembly {

    HG19("hg19"),
    HG38("hg38");

    private final String value;

    GenomeAssembly(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
