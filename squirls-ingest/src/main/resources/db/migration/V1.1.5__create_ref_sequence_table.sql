-- REF_SEQUENCE
drop table if exists SPLICING.REF_SEQUENCE;

create table SPLICING.REF_SEQUENCE
(
    SYMBOL char(50) not null,
    CONTIG       int      not null,-- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS    int      not null,-- 0-based (exclusive) begin position of the region on STRAND
    END_POS      int      not null,-- 0-based (inclusive) end position of the region on STRAND
    STRAND       bool     not null,-- true if FWD, false if REV
    FASTA_SEQUENCE    blob        not null -- FASTA sequence as bytes
);

create unique index REF_SEQUENCE_SYMBOL_index
    on SPLICING.REF_SEQUENCE (SYMBOL);
