drop table if exists SPLICING.CONTIGS;

create table SPLICING.CONTIGS
(
    CONTIG        VARCHAR(50) not null,
    CONTIG_LENGTH INTEGER     not null
);

create index SPLICING.CONTIGS_IDX
    on SPLICING.CONTIGS (CONTIG);

