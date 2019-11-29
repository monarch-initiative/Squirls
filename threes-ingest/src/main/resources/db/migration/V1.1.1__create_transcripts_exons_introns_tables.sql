drop table if exists SPLICING.TRANSCRIPTS;
create table SPLICING.TRANSCRIPTS
(
    CONTIG       int      not null,-- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS    int      not null,-- 0-based (exclusive) begin position of the region
    END_POS      int      not null,-- 0-based (inclusive) end position of the region
    BEGIN_ON_FWD int      not null,-- 0-based (exclusive) begin position of the region on FWD strand
    END_ON_FWD   int      not null,-- 0-based (inclusive) end position of the region on FWD strand
    STRAND       bool     not null,-- true if FWD, false if REV
    TX_ACCESSION char(50) not null -- transcript accession, e.g ENST00000123456.1_1, NM_000404.3
);

create index TRANSCRIPTS_CONTIG_BEGIN_ON_FWD_END_ON_FWD_index
    on SPLICING.TRANSCRIPTS (CONTIG, BEGIN_ON_FWD, END_ON_FWD);


drop table if exists SPLICING.FEATURE_REGIONS;
create table SPLICING.FEATURE_REGIONS
(
    CONTIG        int      not null, -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS     int      not null, -- 0-based (exclusive) begin position of the region
    END_POS       int      not null, -- 0-based (inclusive) end position of the region
    TX_ACCESSION  char(50) not null, -- foreign key from SPLICING.TRANSCRIPTS
    REGION_TYPE   char(2)  not null, -- `ex` if the region is exon, `ir` if the region is intron
    REGION_NUMBER int      not null, -- 0-based number of the region within transcript, e.g. 0 if exon is the first exon in the transcript
    PROPERTIES    varchar(1000)      -- place to store data for the region, expected format is `key1=value1;key2=value2`...
);

create index SPLICING.FEATURE_REGIONS_CONTIG_BEGIN_POS_END_POS_INDEX
    on SPLICING.FEATURE_REGIONS (CONTIG, BEGIN_POS, END_POS);
create index SPLICING.FEATURE_REGIONS_TX_ACCESSION_INDEX
    on SPLICING.FEATURE_REGIONS (TX_ACCESSION);


-- EXONS
-- DROP TABLE IF EXISTS SPLICING.EXONS;
-- CREATE TABLE SPLICING.EXONS
-- (
--     TX_ACCESSION VARCHAR(50) NOT NULL,
--     BEGIN_POS    INTEGER     NOT NULL,
--     END_POS      INTEGER     NOT NULL
-- );
--
-- CREATE INDEX EXONS_IDX ON SPLICING.EXONS (TX_ACCESSION);


-- INTRONS
-- DROP TABLE IF EXISTS SPLICING.INTRONS;
-- CREATE TABLE SPLICING.INTRONS
-- (
--     TX_ACCESSION   VARCHAR(50) NOT NULL,
--     BEGIN_POS      INTEGER     NOT NULL,
--     END_POS        INTEGER     NOT NULL,
--     DONOR_SCORE    DOUBLE      NOT NULL,
--     ACCEPTOR_SCORE DOUBLE      NOT NULL
-- );
--
-- CREATE INDEX INTRONS_IDX ON SPLICING.INTRONS (TX_ACCESSION);
