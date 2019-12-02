create schema if not exists SPLICING;
-- PWM_DATA
drop table if exists SPLICING.PWM_DATA;

create table SPLICING.PWM_DATA
(
    PWM_NAME   VARCHAR(50) not null,
    ROW_IDX    INTEGER     not null,
    COL_IDX    INTEGER     not null,
    CELL_VALUE DOUBLE      not null
);


-- PWM_METADATA
drop table if exists SPLICING.PWM_METADATA;

create table SPLICING.PWM_METADATA
(
    PWM_NAME  VARCHAR(50)  not null,
    PWM_KEY   VARCHAR(200) not null,
    PWM_VALUE VARCHAR(200) not null
);