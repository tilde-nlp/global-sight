# GBS-3054 IDML: URLs not extracted.
ALTER TABLE indd_filter ADD COLUMN TRANSLATE_HYPERLINKS char(1) NOT NULL DEFAULT 'N';