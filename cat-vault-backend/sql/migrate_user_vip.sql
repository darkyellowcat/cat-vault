-- user 表新增 vipLevel 字段
ALTER TABLE `user` ADD COLUMN `vipLevel` INT NOT NULL DEFAULT 0 COMMENT '会员等级: 0-普通 1-付费' AFTER `userRole`;
