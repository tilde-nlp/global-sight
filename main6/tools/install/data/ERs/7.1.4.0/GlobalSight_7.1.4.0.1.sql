-- for GBS-528
ALTER TABLE translation_unit_variant MODIFY COLUMN SID VARCHAR(255);
ALTER TABLE project_tm_tuv_t MODIFY COLUMN SID VARCHAR(255);
ALTER TABLE project_tm_tuv_l MODIFY COLUMN SID VARCHAR(255);
ALTER TABLE page_tm_tuv_t MODIFY COLUMN SID VARCHAR(255);
ALTER TABLE page_tm_tuv_l MODIFY COLUMN SID VARCHAR(255);
commit;