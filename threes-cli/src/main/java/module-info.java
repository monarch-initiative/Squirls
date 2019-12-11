module org.monarchinitiative.threes.cli {
    requires org.monarchinitiative.threes.core;
    requires org.monarchinitiative.threes.autoconfigure;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;

    requires org.slf4j;
    requires argparse4j;

    opens org.monarchinitiative.threes.cli;
    opens org.monarchinitiative.threes.cli.cmd.annotate_pos;
}