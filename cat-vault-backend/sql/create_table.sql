-- Cat Vault 数据库初始化脚本
-- 数据库: cat_vault
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS cat_vault
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cat_vault;

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL COMMENT 'id',
    `userAccount`  VARCHAR(256) NOT NULL COMMENT '账号',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '密码',
    `userName`     VARCHAR(256) NULL     DEFAULT NULL COMMENT '用户昵称',
    `userAvatar`   VARCHAR(1024) NULL    DEFAULT NULL COMMENT '用户头像',
    `userProfile`  VARCHAR(512) NULL     DEFAULT NULL COMMENT '用户简介',
    `userRole`     VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色: user/admin',
    `vipLevel`     INT          NOT NULL DEFAULT 0 COMMENT '会员等级: 0-普通 1-付费',
    `editTime`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `createTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_userAccount` (`userAccount`)
) COMMENT '用户' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 图片表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `picture` (
    `id`            BIGINT        NOT NULL COMMENT 'id',
    `url`           VARCHAR(1024) NOT NULL COMMENT '图片 url',
    `thumbnailUrl`  VARCHAR(1024) NULL     DEFAULT NULL COMMENT '缩略图 url',
    `name`          VARCHAR(256)  NOT NULL COMMENT '图片名称',
    `introduction`  VARCHAR(1024) NULL     DEFAULT NULL COMMENT '简介',
    `category`      VARCHAR(64)   NULL     DEFAULT NULL COMMENT '分类',
    `tags`          VARCHAR(1024) NULL     DEFAULT NULL COMMENT '标签（JSON 数组）',
    `picSize`       BIGINT        NULL     DEFAULT NULL COMMENT '图片体积（字节）',
    `picWidth`      INT           NULL     DEFAULT NULL COMMENT '图片宽度',
    `picHeight`     INT           NULL     DEFAULT NULL COMMENT '图片高度',
    `picScale`      DOUBLE        NULL     DEFAULT NULL COMMENT '图片宽高比例',
    `picFormat`     VARCHAR(32)   NULL     DEFAULT NULL COMMENT '图片格式',
    `picColor`      VARCHAR(16)   NULL     DEFAULT NULL COMMENT '图片主色调（十六进制）',
    `userId`        BIGINT        NOT NULL COMMENT '创建用户 id',
    `spaceId`       BIGINT        NULL     DEFAULT NULL COMMENT '空间 id（null 表示公共图库）',
    `reviewStatus`  INT           NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核 1-通过 2-拒绝',
    `reviewMessage` VARCHAR(512)  NULL     DEFAULT NULL COMMENT '审核信息',
    `reviewerId`    BIGINT        NULL     DEFAULT NULL COMMENT '审核人 id',
    `reviewTime`    DATETIME      NULL     DEFAULT NULL COMMENT '审核时间',
    `editTime`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `createTime`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_spaceId` (`spaceId`),
    INDEX `idx_name` (`name`),
    INDEX `idx_category` (`category`),
    INDEX `idx_reviewStatus` (`reviewStatus`),
    INDEX `idx_createTime` (`createTime`)
) COMMENT '图片' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 空间表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `space` (
    `id`         BIGINT       NOT NULL COMMENT 'id',
    `spaceName`  VARCHAR(128) NOT NULL COMMENT '空间名称',
    `spaceLevel` INT          NOT NULL DEFAULT 0 COMMENT '空间级别: 0-免费 1-专业 2-旗舰',
    `spaceType`  INT          NOT NULL DEFAULT 0 COMMENT '空间类型: 0-私有 1-团队',
    `maxSize`    BIGINT       NOT NULL DEFAULT 0 COMMENT '空间最大容量（字节）',
    `maxCount`   BIGINT       NOT NULL DEFAULT 0 COMMENT '空间最大图片数',
    `totalSize`  BIGINT       NOT NULL DEFAULT 0 COMMENT '已使用容量（字节）',
    `totalCount` BIGINT       NOT NULL DEFAULT 0 COMMENT '已有图片数',
    `userId`     BIGINT       NOT NULL COMMENT '创建用户 id',
    `editTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_spaceName` (`spaceName`),
    INDEX `idx_spaceLevel` (`spaceLevel`)
) COMMENT '空间' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 空间成员表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `space_user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `spaceId`    BIGINT       NOT NULL COMMENT '空间 id',
    `userId`     BIGINT       NOT NULL COMMENT '用户 id',
    `spaceRole`  VARCHAR(32)  NOT NULL DEFAULT 'viewer' COMMENT '空间角色: viewer/editor/admin',
    `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_spaceId_userId` (`spaceId`, `userId`),
    INDEX `idx_userId` (`userId`)
) COMMENT '空间成员' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 站内消息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `message` (
    `id`         BIGINT        NOT NULL COMMENT 'id',
    `userId`     BIGINT        NOT NULL COMMENT '接收用户 id',
    `title`      VARCHAR(256)  NOT NULL COMMENT '消息标题',
    `content`    VARCHAR(1024) NULL     DEFAULT NULL COMMENT '消息内容',
    `hasRead`    TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `createTime` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_userId_hasRead` (`userId`, `hasRead`)
) COMMENT '站内消息' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 为 picture 表添加 fileHash 字段（去重用）
-- ----------------------------
ALTER TABLE `picture` ADD COLUMN `fileHash` VARCHAR(64) NULL DEFAULT NULL COMMENT '文件哈希' AFTER `picColor`;
ALTER TABLE `picture` ADD INDEX `idx_fileHash` (`fileHash`);

