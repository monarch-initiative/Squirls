create schema if not exists SPLICING;

-- CLASSIFIER
drop table if exists SPLICING.CLASSIFIER;

create table SPLICING.CLASSIFIER
(
    VERSION varchar(50) not null,
    DATA    blob        not null
);

create unique index CLASSIFIER_VERSION_UINDEX
    on SPLICING.CLASSIFIER (VERSION);

