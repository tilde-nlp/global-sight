-- for GBS-570
ALTER TABLE TM_PROFILE ADD COLUMN MT_ENGINE VARCHAR(60) DEFAULT NULL;
ALTER TABLE TM_PROFILE ADD COLUMN MT_OVERRIDE_MATCHES CHAR(1) DEFAULT 'N';
ALTER TABLE TM_PROFILE ADD COLUMN MT_AUTOCOMMIT_TO_TM CHAR(1) DEFAULT 'N';
ALTER TABLE TM_PROFILE ADD COLUMN MT_SHOW_IN_EDITOR CHAR(1) DEFAULT 'N';
commit;