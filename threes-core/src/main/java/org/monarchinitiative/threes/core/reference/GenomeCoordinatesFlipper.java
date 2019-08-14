package org.monarchinitiative.threes.core.reference;

import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class GenomeCoordinatesFlipper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenomeCoordinatesFlipper.class);

    private final Map<String, Integer> contigLengthMap;

    public GenomeCoordinatesFlipper(Map<String, Integer> contigLengthMap) {
        this.contigLengthMap = contigLengthMap;
    }

    public Optional<GenomeCoordinates> flip(GenomeCoordinates coordinates) {
        if (!contigLengthMap.containsKey(coordinates.getContig())) {
            LOGGER.warn("Unknown contig {}", coordinates.getContig());
            return Optional.empty();
        }
        final Integer length = contigLengthMap.get(coordinates.getContig());

        final GenomeCoordinates flippedCoordinates = GenomeCoordinates.newBuilder()
                .setContig(coordinates.getContig())
                .setBegin(length - coordinates.getEnd())
                .setEnd(length - coordinates.getBegin())
                .setStrand(!coordinates.getStrand())
                .build();

        return Optional.of(flippedCoordinates);
    }

    public Optional<SplicingVariant> flip(SplicingVariant variant) {
        Optional<GenomeCoordinates> coordinatesOp = flip(variant.getCoordinates());

        return coordinatesOp.map(coors -> SplicingVariant.newBuilder()
                .setCoordinates(coors)
                .setRef(SequenceInterval.reverseComplement(variant.getRef()))
                .setAlt(SequenceInterval.reverseComplement(variant.getAlt()))
                .build());
    }

    public Optional<SequenceInterval> flip(SequenceInterval sequenceInterval) {
        Optional<GenomeCoordinates> coordinatesOp = flip(sequenceInterval.getCoordinates());

        return coordinatesOp.map(coors -> SequenceInterval.of(coors, SequenceInterval.reverseComplement(sequenceInterval.getSequence())));
    }
}
