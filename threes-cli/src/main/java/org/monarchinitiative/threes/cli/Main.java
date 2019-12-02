package org.monarchinitiative.threes.cli;

import org.monarchinitiative.threes.autoconfigure.EnableThrees;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 */
@SpringBootApplication
@EnableThrees
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
