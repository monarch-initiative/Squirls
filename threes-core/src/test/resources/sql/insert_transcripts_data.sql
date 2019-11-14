truncate table splicing.transcripts;
insert into splicing.transcripts (contig, begin_pos, end_pos, begin_on_fwd, end_on_fwd, strand, tx_accession) values

('chr1', 1000, 2000, 1000, 2000, TRUE, 'FIRST'),
('chr1', 5000, 6000, 5000, 6000, TRUE, 'SECOND'),
('chr1', 8000, 10000, 8000, 10000, TRUE, 'THIRD');

truncate table splicing.exons;
insert into splicing.exons (tx_accession, begin_pos, end_pos) values
('FIRST', 1000, 1200),
('FIRST', 1400, 1600),
('FIRST', 1800, 2000),
('SECOND', 5000, 5100),
('SECOND', 5300, 5500),
('SECOND', 5800, 5900),
('SECOND', 5950, 6000),
('THIRD', 8000, 8200),
('THIRD', 8300, 8500),
('THIRD', 8900, 9600),
('THIRD', 9800, 10000);

-- INTRONS
truncate table splicing.introns;
insert into splicing.introns (tx_accession, begin_pos, end_pos, donor_score, acceptor_score) values
('FIRST', 1200, 1400, 9.433, 7.392),
('FIRST', 1600, 1800, 4.931, 7.832),
('SECOND', 5100, 5300, 5.329, 3.848),
('SECOND', 5500, 5800, 9.740, 6.348),
('SECOND', 5900, 5950, 5.294, 8.239),
('THIRD', 8200, 8300, 8.429, 4.541),
('THIRD', 8500, 8900, 5.249, 2.946),
('THIRD', 9600, 9800, 4.234, 1.493);

