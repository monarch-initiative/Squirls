-- CLASSIFIER
drop table if exists SPLICING.CLASSIFIER;

create table SPLICING.CLASSIFIER
(
    VERSION varchar(50) not null,
    DATA    blob        not null
);

create unique index CLASSIFIER_VERSION_UINDEX
    on SPLICING.CLASSIFIER (VERSION);

-- classifier metadata

drop table if exists SPLICING.CLASSIFIER_METADATA;
create table SPLICING.CLASSIFIER_METADATA
(
    VERSION  varchar(50) not null,
    CLF_TYPE varchar(50) not null,
    METADATA clob        not null
);

create unique index CLASSIFIER_METADATA_VERSION_UINDEX
    on SPLICING.CLASSIFIER_METADATA (VERSION);