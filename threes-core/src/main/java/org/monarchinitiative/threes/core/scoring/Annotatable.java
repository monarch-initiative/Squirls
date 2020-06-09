package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.Metadata;
import org.monarchinitiative.threes.core.classifier.transform.feature.MutableFeature;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * This interface describes objects that can be annotated with features by {@link SplicingAnnotator} before being
 * subjected to prediction by {@link org.monarchinitiative.threes.core.classifier.SquirlsClassifier}.
 */
public interface Annotatable extends MutableFeature {

    GenomeVariant getVariant();

    SplicingTranscript getTranscript();

    SequenceInterval getSequence();

    Metadata getMetadata();

    void setMetadata(Metadata metadata);

}
