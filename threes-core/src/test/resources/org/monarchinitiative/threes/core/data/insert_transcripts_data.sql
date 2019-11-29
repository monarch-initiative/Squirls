-- this data is entered with respect to reference dictionary from `TestDataSourceConfig`, where `chr1` = 1 and it has
-- length of 10_000 bp

truncate table SPLICING.TRANSCRIPTS;
insert into SPLICING.TRANSCRIPTS (contig, begin_pos, end_pos, begin_on_fwd, end_on_fwd, strand, tx_accession)
values (1, 1000, 2000, 1000, 2000, TRUE, 'FIRST'),
       (1, 5000, 6000, 5000, 6000, TRUE, 'SECOND'),
       (1, 8000, 10000, 0, 2000, FALSE, 'THIRD');

truncate table SPLICING.FEATURE_REGIONS;
insert into splicing.FEATURE_REGIONS (CONTIG, BEGIN_POS, END_POS, TX_ACCESSION, REGION_TYPE, PROPERTIES, REGION_NUMBER)
values (1, 1000, 1200, 'FIRST', 'ex', '', 0),                            -- 1st exon
       (1, 1200, 1400, 'FIRST', 'ir', 'DONOR=9.433;ACCEPTOR=7.392', 0),  -- 1st intron
       (1, 1400, 1600, 'FIRST', 'ex', '', 1),                            -- 2nd exon
       (1, 1600, 1800, 'FIRST', 'ir', 'DONOR=4.931;ACCEPTOR=7.832', 1),  -- 2nd intron
       (1, 1800, 2000, 'FIRST', 'ex', '', 2),                            -- 3rd exon

       (1, 5000, 5100, 'SECOND', 'ex', '', 0),                           -- 1st exon
       (1, 5100, 5300, 'SECOND', 'ir', 'DONOR=5.329;ACCEPTOR=3.848', 0), -- 1st intron
       (1, 5300, 5500, 'SECOND', 'ex', '', 1),                           -- 2nd exon
       (1, 5500, 5800, 'SECOND', 'ir', 'DONOR=9.740;ACCEPTOR=6.348', 1), -- 2nd intron
       (1, 5800, 5900, 'SECOND', 'ex', '', 2),                           -- 3rd exon
       (1, 5900, 5950, 'SECOND', 'ir', 'DONOR=5.294;ACCEPTOR=8.239', 2), -- 3rd intron
       (1, 5950, 6000, 'SECOND', 'ex', '', 3),                           -- 4th exon

       (1, 8000, 8200, 'THIRD', 'ex', '', 0),                            -- 1st exon
       (1, 8200, 8300, 'THIRD', 'ir', 'DONOR=8.429;ACCEPTOR=4.541', 0),  -- 1st intron
       (1, 8300, 8500, 'THIRD', 'ex', '', 1),                            -- 2nd exon
       (1, 8500, 8900, 'THIRD', 'ir', 'DONOR=5.249;ACCEPTOR=2.946', 1),  -- 2nd intron
       (1, 8900, 9600, 'THIRD', 'ex', '', 2),                            -- 3rd exon
       (1, 9600, 9800, 'THIRD', 'ir', 'DONOR=4.234;ACCEPTOR=1.493', 2),  -- 3rd intron
       (1, 9800, 10000, 'THIRD', 'ex', '', 3);

