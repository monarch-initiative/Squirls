truncate table SPLICING.REF_DICT_ID_NAME;
insert into SPLICING.REF_DICT_ID_NAME(ID, NAME)
values (1, 'chr1'),
       (2, 'chr2'),
       (3, 'chrX');

truncate table SPLICING.REF_DICT_NAME_ID;
insert into SPLICING.REF_DICT_NAME_ID(NAME, ID)
VALUES ('chr1', 1),
       ('chr2', 2),
       ('chrX', 3);


truncate table SPLICING.REF_DICT_ID_LENGTH;
insert into SPLICING.REF_DICT_ID_LENGTH(ID, LENGTH)
values (1, 10000),
       (2, 20000),
       (3, 30000);