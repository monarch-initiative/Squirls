package org.monarchinitiative.squirls.core.data.kmer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 */
public class FileKMerParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileKMerParser.class);

    private final Map<String, Double> kmerMap;

    public FileKMerParser(Path kmerTsvPath) throws IOException {
        kmerMap = parseKmerTsvFile(kmerTsvPath);
    }

    private static Map<String, Double> parseKmerTsvFile(Path kmerTsvPath) throws IOException {
        Map<String, Double> kmerMap = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(kmerTsvPath)) {
            reader.lines()
                    .filter(emptyLines()).filter(comments())
                    .forEach(line -> {
                        final String[] tokens = line.split("\t");
                        if (tokens.length != 2) {
                            LOGGER.warn("Invalid line {}", line);
                            return;
                        }
                        final String seq = tokens[0].toUpperCase();

                        final double score;
                        try {
                            score = Double.parseDouble(tokens[1]);
                        } catch (NumberFormatException e) {
                            LOGGER.warn("Invalid score {} in line {}", tokens[1], line);
                            return;
                        }
                        kmerMap.put(seq, score);
                    });

        }
        return kmerMap;
    }

    private static Predicate<? super String> comments() {
        return line -> !line.startsWith("#");
    }

    private static Predicate<? super String> emptyLines() {
        return line -> !line.isEmpty();
    }

    public Map<String, Double> getKmerMap() {
        return kmerMap;
    }
}
