create schema if not exists SPLICING;

-- PHYLOP conservation
drop table if exists SPLICING.PHYLOP_SCORE;

create table SPLICING.PHYLOP_SCORE
(
    SYMBOL           char(50)    not null,
    CONTIG           int         not null,  -- contig id which maps to `SPLICING.REF_DICT_ID_NAME.ID` and `SPLICING.REF_DICT_NAME_ID.ID`
    BEGIN_POS        int         not null,  -- 0-based (exclusive) begin position of the region on STRAND
    END_POS          int         not null,  -- 0-based (inclusive) end position of the region on STRAND
    STRAND           bool        not null,  -- true if FWD, false if REV
    PHYLOP_VALUES    blob        not null   -- PHYLOP values as bytes
);

create unique index PHYLOP_SCORE_SYMBOL_index
    on SPLICING.PHYLOP_SCORE (SYMBOL);

