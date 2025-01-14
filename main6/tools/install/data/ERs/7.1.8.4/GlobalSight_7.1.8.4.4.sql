# For GBS-1433
# 1 Add an sso account mapping
CREATE TABLE IF NOT EXISTS SSO_USER_MAPPING
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  COMPANY_ID BIGINT NOT NULL,
  USER_ID VARCHAR(80) NOT NULL,
  SSO_USER_ID VARCHAR(100)
) AUTO_INCREMENT=1 ENGINE=InnoDB DEFAULT CHARSET=utf8;

# 2 Add option for company
ALTER TABLE `company` ADD COLUMN `ENABLE_SSO_LOGIN` char(1) DEFAULT 'N' NOT NULL AFTER `ENABLE_IP_FILTER`;
ALTER TABLE `company` ADD COLUMN `SSO_IDP_URL` VARCHAR(256) NULL AFTER `ENABLE_SSO_LOGIN`;
ALTER TABLE `company` ADD COLUMN `SSO_LOGIN_URL` VARCHAR(256) NULL AFTER `SSO_IDP_URL`;
ALTER TABLE `company` ADD COLUMN `SSO_LOGOUT_URL` VARCHAR(256) NULL AFTER `SSO_LOGIN_URL`;
ALTER TABLE `company` ADD COLUMN `SSO_WS_ENDPOINT` VARCHAR(256) NULL AFTER `SSO_LOGOUT_URL`;

