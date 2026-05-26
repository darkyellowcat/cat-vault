# Cat Vault - 云图库模板框架

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)
![React](https://img.shields.io/badge/React-18-blue.svg)
![Ant Design](https://img.shields.io/badge/Ant%20Design-5-1890ff.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

一套开箱即用的云图库模板框架，包含完整的前后端实现。支持团队协作空间、图片审核、颜色搜索、在线编辑等功能。

</div>

---

## 项目简介

Cat Vault 是一个可复用的云图库模板框架，开发者可以基于此模板快速搭建自己的图片管理平台。项目包含：

- **cat-vault-backend** — Spring Boot 3.2 后端服务
- **cat-vault-frontend** — React 18 + Ant Design 5 前端应用

### 核心能力

- 用户系统（注册/登录/角色管理）
- 图片全生命周期管理（上传/编辑/审核/删除）
- 团队协作空间（创建空间/成员管理/配额控制）
- 图片颜色搜索（主色调提取 + RGB 距离排序）
- 图片在线编辑（裁剪/旋转/缩放/水印/格式转换）
- 多级缓存（Caffeine + Redis）
- 事件驱动通知系统
- 图片去重（SHA-256 哈希）
- 接口限流（Redis 滑动窗口）
- 操作审计日志

---

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.2 | 基础框架 |
| MyBatis-Plus 3.5.9 | ORM + 分页 + 逻辑删除 |
| MySQL 8.0 | 关系型数据库 |
| Redis | 分布式缓存 + Session + 限流 |
| Caffeine | 本地缓存 |
| 腾讯云 COS | 对象存储（含数据万象图片处理） |
| Spring AOP | 权限拦截 / 限流 / 审计日志 |
| BCrypt | 密码加密 |
| Knife4j | API 文档 |
| Spring Boot Actuator | 健康检查 + 监控指标 |

### 前端

| 技术 | 说明 |
|------|------|
| React 18 | UI 框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Ant Design 5 | UI 组件库 |
| React Router 6 | 路由 |
| Axios | HTTP 请求 |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 方式一：Docker 一键启动（推荐）

```bash
cd cat-vault-backend
cp .env.example .env
# 编辑 .env 填入数据库密码和 COS 配置
docker-compose up -d
```

### 方式二：本地开发

以下命令默认从项目根目录执行：

```bash
# 1. 初始化数据库
mysql -u root -p < cat-vault-backend/sql/create_table.sql

# 如果使用旧库升级，需要执行 VIP 字段迁移
mysql -u root -p cat_vault < cat-vault-backend/sql/migrate_user_vip.sql

# 2. 配置本地 COS
# 在 cat-vault-backend/src/main/resources/application-local.yml 中填写 COS 配置
# application-local.yml 已被 Git 忽略，不要提交真实密钥

# 3. 启动后端
cd cat-vault-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. 启动前端
cd ../cat-vault-frontend
npm install
npm run dev
```

启动后访问：
- 前端：http://localhost:3000
- 后端 API：http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html
- 健康检查：http://localhost:8080/api/actuator/health

---

## 项目结构

```
cat-vault/
├── cat-vault-backend/          # 后端服务
│   ├── sql/                    # 数据库建表脚本
│   ├── src/main/java/.../
│   │   ├── annotation/         # @AuthCheck, @RateLimit, @AuditLog
│   │   ├── aop/                # 权限/限流/审计 拦截器
│   │   ├── config/             # CORS, COS, MyBatis, JSON
│   │   ├── controller/         # REST 控制器
│   │   ├── event/              # 事件驱动（审核通知）
│   │   ├── manager/            # COS 管理 + 上传模板
│   │   ├── model/              # entity / dto / vo / enums
│   │   └── service/            # 业务逻辑
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── .env.example
│
└── cat-vault-frontend/         # 前端应用
    ├── src/
    │   ├── layouts/            # 页面布局
    │   ├── pages/              # 页面组件
    │   ├── services/           # API 请求层
    │   └── utils/              # 工具函数
    ├── vite.config.ts
    └── package.json
```

---

## API 接口

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /register | 用户注册 | 公开 |
| POST | /login | 用户登录 | 公开 |
| POST | /get/login | 获取当前用户 | 登录 |
| POST | /logout | 退出登录 | 登录 |
| POST | /add | 创建用户 | 管理员 |
| POST | /delete | 删除用户 | 管理员 |
| POST | /update | 更新用户 | 管理员 |
| POST | /list/page/vo | 分页查询用户 | 管理员 |

### 图片模块 `/api/picture`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /upload | 文件上传 | 管理员 |
| POST | /upload/url | URL 上传 | 管理员 |
| POST | /upload/batch | 批量抓取 | 管理员 |
| POST | /delete | 删除图片 | 所有者/管理员 |
| POST | /edit | 编辑图片信息 | 所有者 |
| POST | /edit/image | 在线编辑图片 | 所有者/管理员 |
| GET | /get/vo | 获取图片详情 | 公开 |
| POST | /list/page/vo | 分页查询 | 公开 |
| POST | /list/page/vo/cache | 分页查询（带缓存） | 公开 |
| GET | /search/color | 颜色搜索 | 公开 |
| GET | /tag_category | 标签分类 | 公开 |
| POST | /review | 审核图片 | 管理员 |

### 空间模块 `/api/space`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /add | 创建空间 | 登录 |
| POST | /delete | 删除空间 | 创建者/管理员 |
| POST | /update | 更新空间 | 创建者/管理员 |
| GET | /get/vo | 获取空间详情 | 公开 |
| POST | /list/page/vo | 分页查询空间 | 公开 |
| POST | /member/add | 添加成员 | 空间创建者 |
| POST | /member/remove | 移除成员 | 空间创建者 |
| POST | /member/list | 成员列表 | 登录 |
| GET | /my/list | 我的空间 | 登录 |

### 消息模块 `/api/message`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /my/list | 我的消息 | 登录 |
| GET | /my/unread/count | 未读数 | 登录 |
| POST | /read | 标记已读 | 登录 |

---

## 前端页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录/注册 | /login | 账号密码登录 + 注册 |
| 图库浏览 | / | 网格布局、搜索、分类筛选、分页 |
| 图片详情 | /picture/:id | 大图预览、元数据、删除 |
| 图片上传 | /upload | 拖拽上传 + URL 上传 |
| 空间管理 | /spaces | 创建空间、我的空间列表 |
| 管理后台 | /admin | 用户管理 + 图片审核 |

---

## 多环境配置

| Profile | 用途 | 说明 |
|---------|------|------|
| local | 本地开发 | 使用本地 MySQL/Redis；COS 密钥放在未提交的 application-local.yml 中 |
| dev | 开发环境 | 敏感配置通过环境变量注入 |
| prod | 生产环境 | 关闭 SQL 日志和 API 文档，HikariCP 连接池调优 |

---

## 安全特性

- BCrypt 密码加密
- Redis 滑动窗口接口限流（`@RateLimit`）
- SQL 注入防护（排序字段白名单）
- SSRF 防护（URL 上传内网地址拦截）
- 路径遍历防护
- Bean Validation 参数校验
- CORS 按环境配置
- 操作审计日志（`@AuditLog`）

---

## 许可证

MIT License
