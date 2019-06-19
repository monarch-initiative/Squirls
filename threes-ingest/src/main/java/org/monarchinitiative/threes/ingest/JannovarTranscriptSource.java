package org.monarchinitiative.threes.ingest;

public enum JannovarTranscriptSource {

    REFSEQ("refseq"),
    REFSEQ_CURATED("refseq_curated"),
    ENSEMBL("ensembl"),
    UCSC("ucsc");

    private final String value;

    JannovarTranscriptSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
