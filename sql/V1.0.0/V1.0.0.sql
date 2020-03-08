
ALTER TABLE `litemall`.`litemall_user`
ADD COLUMN `id_card` CHAR(18) NOT NULL DEFAULT '' COMMENT '身份证号' AFTER `deleted`,
ADD COLUMN `type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户类型：1-普通用户，2-公司用户' AFTER `id_card`,
ADD COLUMN `company` VARCHAR(100) NULL DEFAULT '' COMMENT '公司名称' AFTER `type`,
ADD COLUMN `company_address` VARCHAR(100) NULL DEFAULT '' COMMENT '公司地址' AFTER `company`;
