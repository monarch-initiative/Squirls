package org.monarchinitiative.sss.core.reference;

import org.monarchinitiative.sss.core.model.GenomeCoordinates;
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
        final GenomeCoordinates flippedCoordinates;
        try {
            flippedCoordinates = GenomeCoordinates.newBuilder()
                    .setContig(coordinates.getContig())
                    .setBegin(length - coordinates.getEnd())
                    .setEnd(length - coordinates.getBegin())
                    .setStrand(!coordinates.isStrand())
                    .build();
        } catch (InvalidCoordinatesException e) {
            LOGGER.warn("Invalid coordinates - {}:{}-{}", coordinates.getContig(), coordinates.getBegin(), coordinates.getEnd());
            return Optional.empty();
        }
        return Optional.of(flippedCoordinates);
    }
}
