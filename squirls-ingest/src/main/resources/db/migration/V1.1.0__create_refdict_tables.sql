-- ----------- mapping IDs to primary name ----------------------------
DROP TABLE IF EXISTS SPLICING.REF_DICT_ID_NAME;
CREATE TABLE SPLICING.REF_DICT_ID_NAME
(
    ID   INT          NOT NULL, -- numerical id unique for a chromosome
    NAME VARCHAR(100) NOT NULL  -- chromosome name, such as 'chrX', 'X', or 'chr16_KI270728v1_random'
);
CREATE INDEX IDS_IDX ON SPLICING.REF_DICT_ID_NAME (ID);

-- ----------- mapping ID to length ------------------------------------
DROP TABLE IF EXISTS SPLICING.REF_DICT_ID_LENGTH;
CREATE TABLE SPLICING.REF_DICT_ID_LENGTH
(
    ID     INT NOT NULL, -- numerical id unique for a chromosome
    LENGTH INT NOT NULL  -- chromosome length in base pairs
);
CREATE INDEX LENGTHS_IDX ON SPLICING.REF_DICT_ID_LENGTH (ID);

-- ----------- mapping names to ID ------------------------------------
DROP TABLE IF EXISTS SPLICING.REF_DICT_NAME_ID;
CREATE TABLE SPLICING.REF_DICT_NAME_ID
(
    NAME VARCHAR(100) NOT NULL, -- chromosome name, such as 'chrX', 'X', or 'chr16_KI270728v1_random'
    ID   INT          NOT NULL  -- numerical id unique for a chromosome
);
CREATE INDEX NAMES_IDX ON SPLICING.REF_DICT_NAME_ID (ID);