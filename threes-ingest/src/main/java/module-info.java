module org.monarchinitiative.threes.ingest {
    requires org.monarchinitiative.threes.core;
    requires exomiser.core;
    requires jannovar.core;

    requires xyz.ielis.hyperutil.reference;
    requires htsjdk;
    requires jblas;
    requires commons.io;
    requires com.google.common;
    requires org.apache.commons.compress;

    requires spring.core;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.flywaydb.core;

    requires org.slf4j;

    opens org.monarchinitiative.threes.ingest;
    opens org.monarchinitiative.threes.ingest.pwm;
    opens org.monarchinitiative.threes.ingest.reference;
    opens org.monarchinitiative.threes.ingest.transcripts;
}