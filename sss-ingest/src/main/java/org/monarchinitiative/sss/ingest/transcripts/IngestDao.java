package org.monarchinitiative.sss.ingest.transcripts;


import org.monarchinitiative.sss.core.model.SplicingTranscript;

/**
 *
 */
public interface IngestDao {

    int insertTranscript(SplicingTranscript transcript);
}
