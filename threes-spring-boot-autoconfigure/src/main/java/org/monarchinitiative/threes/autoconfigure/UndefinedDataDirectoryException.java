package org.monarchinitiative.threes.autoconfigure;

/**
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class UndefinedDataDirectoryException extends RuntimeException {

    private String value;

    public UndefinedDataDirectoryException(String value) {
        super(String.format("Invalid data directory '%s'", value));
    }

    public String getValue() {
        return value;
    }

}
