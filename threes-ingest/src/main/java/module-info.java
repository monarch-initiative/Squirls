module org.monarchinitiative.threes.ingest {
    requires org.monarchinitiative.threes.core;

    requires jannovar.core;
    requires com.google.common;
    requires jblas;
    requires commons.io;
    requires org.apache.commons.compress;
    requires htsjdk;

    requires spring.core;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.flywaydb.core;

    requires slf4j.api;

    opens org.monarchinitiative.threes.ingest;
    opens org.monarchinitiative.threes.ingest.pwm;
    opens org.monarchinitiative.threes.ingest.reference;
    opens org.monarchinitiative.threes.ingest.transcripts;
    opens db.migration;
}