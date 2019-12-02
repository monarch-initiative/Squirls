module org.monarchinitiative.threes.autoconfigure {
    exports org.monarchinitiative.threes.autoconfigure;

    requires org.monarchinitiative.threes.core;
    requires xyz.ielis.hyperutil.reference;

    requires com.google.common;

    requires spring.boot;
    requires spring.core;
    requires spring.context;
    requires spring.boot.autoconfigure;

    requires java.sql;
    requires com.zaxxer.hikari;

    requires org.slf4j;

    opens org.monarchinitiative.threes.autoconfigure;
}