#For GBS-1953
ALTER TABLE `removed_prefix_tag` CHANGE COLUMN `STRING` `STRING` MEDIUMTEXT NOT NULL;
ALTER TABLE `removed_suffix_tag` CHANGE COLUMN `STRING` `STRING` MEDIUMTEXT NOT NULL;
ALTER TABLE `removed_tag` CHANGE COLUMN `PREFIX_STRING` `PREFIX_STRING` MEDIUMTEXT NOT NULL;
ALTER TABLE `removed_tag` CHANGE COLUMN `SUFFIX_STRING` `SUFFIX_STRING` MEDIUMTEXT NOT NULL;