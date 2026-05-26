# Cat Vault - 云图库模板框架

基于 Spring Boot 3 + React 18 的企业级云图库管理系统，支持多空间、团队协作、图片审核、VIP 权限等功能。

## 技术栈

**后端**
- Spring Boot 3.2.2 / Java 17
- MyBatis-Plus 3.5.9
- MySQL 8.0 + Redis
- 腾讯云 COS（对象存储）
- Spring Session（Redis 分布式会话）
- Knife4j（OpenAPI 3 文档）

**前端**
- React 18 + TypeScript
- Vite 构建
- Ant Design 5 + Ant Design Pro Components

## 核心功能

- 用户注册/登录，BCrypt 密码加密
- 图片上传（文件/URL），支持填写名称、简介、分类、标签
- WebP 压缩、缩略图生成、主色调提取
- 图片审核流程（待审核/通过/拒绝），空间内图片自动过审
- 多空间管理（私有/团队），成员角色（viewer/editor/admin）
- VIP 等级权限控制（普通用户最多 3 个免费空间，付费用户可创建专业版/旗舰版）
- 按颜色搜索（RGB 欧氏距离）、关键词搜索（名称+简介模糊匹配）、标签分类筛选
- 分页缓存（Caffeine + Redis 双层缓存，上传/删除/审核自动失效）
- 站内消息通知（审核结果、空间成员变动、上传成功）
- 管理员后台（用户管理、图片管理、空间管理、VIP 提权）

## 项目结构

```
cat-vault/
├── cat-vault-backend/       # Spring Boot 后端
│   ├── sql/                 # 数据库初始化与迁移脚本
│   └── src/main/java/...   # 业务代码
├── cat-vault-frontend/      # React 前端
│   └── src/
│       ├── pages/           # 页面组件
│       ├── services/        # API 调用层
│       └── utils/           # 工具函数
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- Redis 6+

### 数据库初始化

```bash
mysql -u root -p < cat-vault-backend/sql/create_table.sql

# 如果使用旧库升级，需要执行 VIP 字段迁移
mysql -u root -p cat_vault < cat-vault-backend/sql/migrate_user_vip.sql
```

### 后端启动

```bash
cd cat-vault-backend

# 默认连接本地 MySQL cat_vault、Redis 127.0.0.1:6379
# 数据库默认账号为 root/123456，可通过环境变量 DB_USERNAME、DB_PASSWORD 等覆盖
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

本地开发的 COS 密钥放在 `cat-vault-backend/src/main/resources/application-local.yml`，该文件已被 Git 忽略。Docker 启动时可复制 `cat-vault-backend/.env.example` 为 `.env` 后填写真实配置。

### 前端启动

```bash
cd cat-vault-frontend
npm install
npm run dev
```

默认前端运行在 http://localhost:3000，后端 API 在 http://localhost:8080/api。

## 权限模型

| 角色 | 能力 |
|------|------|
| 游客 | 浏览公共图库、搜索图片、注册、登录 |
| 普通用户 (vipLevel=0) | 在空间内上传/编辑/删除图片，创建最多 3 个免费空间，查看消息 |
| 付费用户 (vipLevel>=1) | 创建专业版/旗舰版空间，最多 10 个空间 |
| 管理员 | 上传到公共图库、图片审核、用户管理、空间管理、VIP 提权 |

## 安全特性

- BCrypt 密码加密
- AOP 权限拦截（@AuthCheck）
- SQL 注入防护（排序字段白名单）
- SSRF 防护（URL 上传内网地址拦截）
- 路径遍历防护（文件下载前缀校验）
- 接口限流（注册/登录/上传）
- Snowflake ID + Jackson Long→String 序列化（避免前端精度丢失）
