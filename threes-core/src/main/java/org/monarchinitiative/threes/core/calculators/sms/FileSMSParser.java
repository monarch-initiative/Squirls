package org.monarchinitiative.threes.core.calculators.sms;

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
public class FileSMSParser implements SMSParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSMSParser.class);

    private final Map<String, Double> septamerMap;

    public FileSMSParser(Path septamerTsvPath) throws IOException {
        septamerMap = parseSeptamerTsvFile(septamerTsvPath);
    }

    private static Map<String, Double> parseSeptamerTsvFile(Path septamerTsvPath) throws IOException {
        Map<String, Double> septamerMap = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(septamerTsvPath)) {
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
                        septamerMap.put(seq, score);
                    });

        }
        return septamerMap;
    }

    private static Predicate<? super String> comments() {
        return line -> !line.startsWith("#");
    }

    private static Predicate<? super String> emptyLines() {
        return line -> !line.isEmpty();
    }

    @Override
    public Map<String, Double> getSeptamerMap() {
        return septamerMap;
    }
}
