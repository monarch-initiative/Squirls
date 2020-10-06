create schema if not exists SPLICING;


-----------------------------------------------------------------------
--
--          GENES & TRANSCRIPTS
--
-----------------------------------------------------------------------
drop table if exists SPLICING.GENE;
create table SPLICING.GENE
(
    CONTIG       int         not null, -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS    int         not null, -- 0-based (exclusive) begin position of the region
    END_POS      int         not null, -- 0-based (inclusive) end position of the region
    BEGIN_ON_FWD int         not null, -- 0-based (exclusive) begin position of the region on FWD strand
    END_ON_FWD   int         not null, -- 0-based (inclusive) end position of the region on FWD strand
    STRAND       bool        not null, -- true if FWD, false if REV
    GENE_ID      int         not null, -- numeric gene id, unique within the schema
    SYMBOL       varchar(50) not null  -- HGVS gene symbol, e.g. FBN1
);
create index GENE_GENE_ID_index
    on SPLICING.GENE (GENE_ID);
create index GENE_CONTIG_BEGIN_END_index
    on SPLICING.GENE (CONTIG, BEGIN_ON_FWD, END_ON_FWD);

drop table if exists SPLICING.GENE_TO_TX;
create table SPLICING.GENE_TO_TX
(
    GENE_ID int not null, -- foreign key to `SPLICING.GENES`
    TX_ID   int not null  -- unique numeric transcript ID
);
create index GENE_TO_TX_GENE_ID_index
    on SPLICING.GENE_TO_TX (GENE_ID);
create index GENE_TO_TX_TX_ID_index
    on SPLICING.GENE_TO_TX (TX_ID);

drop table if exists SPLICING.TRANSCRIPT;
create table SPLICING.TRANSCRIPT
(
    TX_ID        int      not null, -- foreign key to `SPLICING.GENE_TO_TX`
    CONTIG       int      not null, -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS    int      not null, -- 0-based (exclusive) begin position of the region
    END_POS      int      not null, -- 0-based (inclusive) end position of the region
    BEGIN_ON_FWD int      not null, -- 0-based (exclusive) begin position of the region on FWD strand
    END_ON_FWD   int      not null, -- 0-based (inclusive) end position of the region on FWD strand
    STRAND       bool     not null, -- true if FWD, false if REV
    ACCESSION_ID char(50) not null  -- tx accession id, e.g. `NM_123456.5` for RefSeq, `ENST000000123456.7` for ENSEMBL, etc.
);
create unique index TRANSCRIPT_TX_ID_index
    on SPLICING.TRANSCRIPT (TX_ID);
create index TRANSCRIPT_CONTIG_BEGIN_END_index
    on SPLICING.TRANSCRIPT (CONTIG, BEGIN_ON_FWD, END_ON_FWD);



drop table if exists SPLICING.TX_FEATURE_REGION;
create table SPLICING.TX_FEATURE_REGION
(
    TX_ID         int     not null, -- foreign key to `SPLICING.GENE_TO_TX`
    CONTIG        int     not null, -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS     int     not null, -- 0-based (exclusive) begin position of the region
    END_POS       int     not null, -- 0-based (inclusive) end position of the region
    REGION_TYPE   char(2) not null, -- `ex` if the region is exon, `ir` if the region is intron
    REGION_NUMBER int     not null, -- 0-based number of the region within transcript, e.g. 0 if exon is the first exon in the transcript
    PROPERTIES    varchar(1000)     -- place to store data for the region, expected format is `key1=value1;key2=value2`...
);

create index SPLICING.TX_FEATURE_REGION_TX_ID_index
    on SPLICING.TX_FEATURE_REGION (TX_ID);

-----------------------------------------------------------------------
--
--          TRACKS FOR GENES
--
-----------------------------------------------------------------------
-- GENE_TRACKS
drop table if exists SPLICING.GENE_TRACK;
create table SPLICING.GENE_TRACK
(
    GENE_ID        int  not null,
    CONTIG         int  not null, -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS      int  not null, -- 0-based (exclusive) begin position of the region on STRAND
    END_POS        int  not null, -- 0-based (inclusive) end position of the region on STRAND
    STRAND         bool not null, -- true if FWD, false if REV
    FASTA_SEQUENCE blob not null, -- FASTA sequence as bytes
    PHYLOP_VALUES  blob not null  -- PHYLOP values as bytes
);
create unique index GENE_TRACK_GENE_ID_index
    on SPLICING.GENE_TRACK (GENE_ID);
