# 校园实习招聘管理平台

> 面向高校实习招聘场景的全栈管理平台：岗位发布与审核、简历投递、面试安排、实习协议签署、实习周报评价与数据统计，覆盖 **学生 / 企业 / 学校管理员** 三类角色。

<p align="center">
  <b>前端</b> Vue 3 + Element Plus + Pinia + ECharts &nbsp;|&nbsp;
  <b>后端</b> Spring Boot 2.7 + Spring Security(JWT) + MyBatis-Plus &nbsp;|&nbsp;
  <b>数据库</b> MySQL 8
</p>

---

## 📖 项目简介

本平台打通校园实习招聘的全流程闭环：

```
岗位发布 → 管理员审核 → 学生检索 → 简历投递(快照留痕) → 企业筛选
        → 安排面试 → 学生确认 → 面试完成 → 发起实习协议 → 学生签署
        → 提交实习周报 → 企业评分评价
```

学生、企业、管理员三方在线协同，岗位信息统一公示、投递过程可追踪、实习过程留痕可统计；同时提供 **匿名可访问的数据大屏**，实时展示平台运营指标。

整体采用 **前后端分离** 架构：前端为 Vue3 单页应用（学生端 / 企业端 / 管理端三大门户 + 公开大屏），后端对外提供统一风格的 RESTful 接口，认证采用 无状态 JWT + BCrypt。

---

## ✨ 功能特性

| 角色 | 主要功能 |
| --- | --- |
| 👨‍🎓 **学生** | 注册登录 · 岗位检索与详情 · 在线简历（多份 / 默认 / 公开开关） · 一键投递（固化简历快照） · 投递追踪 · 面试确认/取消 · 协议签署/拒绝 · 实习周报提交 · 站内消息 |
| 🏢 **企业** | 注册与资质认证 · 企业档案维护 · 岗位发布/修改/下线 · 申请筛选（通过/淘汰） · 安排面试 · 发起实习协议 · 周报评分与评语 · 站内消息 |
| 🛡️ **管理员** | 企业认证审核 · 岗位合规审核 · 仪表盘与数据分析（趋势/分布/排行/成功率） · 接口文档自动生成 |
| 🌐 **匿名访客** | 岗位检索 / 岗位详情 / 企业详情 · 公开数据大屏（学生信息脱敏） |

**关键业务规则**

- 企业未通过认证 **不能发布岗位**
- 岗位须为 `online`（审核通过）才能被检索与投递
- 同一岗位同一简历 **仅允许一次有效投递**（数据库唯一键 + 服务层双校验）
- 投递时固化 **简历快照（JSON）**，保证历史可回溯
- 招满校验、越权防护（学生只能操作自己的简历/投递，企业只能处理自己岗位的投递）
- 企业关键资料变更后需 **重新认证**

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│  浏览器  Chrome / Edge                                           │
│  Vue3 SPA（学生端 / 企业端 / 管理端 / 数据大屏）                    │
│  vue-cli dev-server :8081  ──代理 /api、/uploads──▶ :8080        │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTPS / RESTful JSON
                                │ Authorization: Bearer <JWT>
┌───────────────────────────────▼─────────────────────────────────┐
│  Spring Boot 2.7 (:8080)                                         │
│  Spring Security + JWT 无状态认证  ·  URL 级 + 方法级 + 数据归属校验 │
│  Controller → Service → Mapper(MyBatis-Plus)                     │
└───────────────────────────────┬─────────────────────────────────┘
                                │ JDBC
┌───────────────────────────────▼─────────────────────────────────┐
│  MySQL 8.x（sxzp 库，12 张业务表）                                │
│  本地附件存储 ./uploads（经 /uploads/** 静态映射）                 │
└─────────────────────────────────────────────────────────────────┘
```

**代码仓库结构**：`backend/`（Spring Boot 多模块后端）与 `frontend/new/`（Vue CLI 前端）为两个独立工程，建议分别建仓托管。

---

## 🧱 技术栈

| 层 | 技术选型 |
| --- | --- |
| **前端** | Vue 3.2（`<script setup>`） · Vue Router 4 · Pinia 2 · Axios 1.6 · Element Plus 2.4 · ECharts 5.4 · dayjs · Sass · Vue CLI 5（Webpack） |
| **后端** | Spring Boot 2.7.18 · Spring Security（无状态 JWT） · MyBatis-Plus 3.5.3.1 · JJWT 0.12.5 · Lombok 1.18.30 · Bean Validation |
| **数据库** | MySQL 8.x（InnoDB / utf8mb4），逻辑外键 + 索引 |
| **构建运行** | JDK 1.8（编译目标，兼容 8/11/17） · Maven 3.6+ · Node.js 16+ |

后端为 Maven 多模块：父 POM `com.sxzp:backend` 统一管理版本，业务全部在 `sxzp-server` 子模块。

---

## 📁 项目结构

```
project0/
├── backend/                          # 后端工程（Maven 多模块）
│   ├── pom.xml                       # 父 POM（统一版本）
│   ├── README.md                     # 后端详细文档
│   └── sxzp-server/
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/sxzp/
│           │   ├── SxzpApplication.java      # 启动类
│           │   ├── common/                   # result / exception / security / config / 接口文档
│           │   └── module/                   # 15 个业务模块（entity/mapper/dto/vo/service/controller）
│           │       ├── auth/ user/ company/ student/ job/ resume/ apply/
│           │       ├── interview/ agreement/ weekly/ notice/ file/ dict/
│           │       └── stats/ admin/
│           └── resources/
│               ├── application.yml           # 数据源 / JWT / 上传 / MyBatis-Plus
│               └── db/                       # schema.sql / migration_incremental.sql / 演示数据
│
├── frontend/new/                     # 前端工程（Vue CLI）
│   ├── README.md                     # 前端详细文档
│   ├── vue.config.js                 # dev:8081，代理 /api、/uploads → :8080
│   └── src/
│       ├── api/                      # 接口封装（request.js + 各模块）
│       ├── components/               # 通用组件（JobCard / DictTag / ChartBase / FileUpload…）
│       ├── composables/              # useDict / usePagination
│       ├── layouts/                  # PublicLayout / BasicLayout
│       ├── router/                   # public / student / company / admin 模块化路由
│       ├── stores/                   # Pinia（auth / dict）
│       ├── utils/                    # enums / menus / format / auth
│       └── views/                    # 公共、学生、企业、管理端页面 + DataScreen 大屏
│
├── docs/                             # 截图等展示素材
├── database_export/                  # 数据库导出（SQL / Excel）
├── 报告/                              # 需求规约、数据库设计、架构设计等课程文档
└── HANDOVER.md                       # 后端工程交接文档
```

---

## 🔄 核心业务流程

### 状态机

| 对象 | 状态流转 |
| --- | --- |
| 企业认证 | `pending` → `auth` / `reject`（`freeze` 冻结） |
| 岗位 | `draft` → `pending` → `online` / `reject`；`online` → `ended`（下线）；`reject`/`draft` → 重新提交 |
| 投递 | `pending` → `interview` / `rejected`（企业处理）→ `agreement`（协议签署）→ `hired`；`withdrawn`（学生撤回） |
| 面试 | `pending` → `confirmed` → `done`；`pending` / `confirmed` → `cancelled` |
| 协议 | `pending` → `signed` / `rejected`（同一投递仅一份有效协议） |
| 周报 | `submitted` → `reviewed`（同一投递同一周次唯一） |

### 全流程时序（简要）

1. 企业注册 → 上传资质 → 管理员审核 → `auth`
2. 企业发布岗位（草稿 / 提交审核）→ 管理员审核通过 → `online` 上线
3. 学生检索岗位 → 投递（生成简历快照、防重复、防招满）
4. 企业筛选投递 → 安排面试（站内信通知）→ 学生确认 → 面试完成
5. 企业发起实习协议 → 学生签署 → 投递进入 `agreement`
6. 学生实习期内按周提交周报 → 企业评分与评语 → 数据进入统计

---

## 🗄️ 数据库设计（12 张表）

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 统一账号（student / company / admin），BCrypt 口令 |
| `sys_role` | 角色定义（RBAC） |
| `company` | 企业档案与认证（pending / auth / reject / freeze） |
| `student` | 学生档案（学号唯一） |
| `job` | 实习岗位（draft / pending / online / reject / ended） |
| `student_resume` | 学生简历（多份，默认简历唯一，可设置公开） |
| `job_apply` | 投递记录（含简历快照 JSON，`uk_apply_job_resume` 防重复） |
| `interview` | 面试安排（pending / confirmed / cancelled / done） |
| `agreement` | 实习协议（pending / signed / rejected） |
| `weekly_report` | 实习周报与评价（submitted / reviewed） |
| `notice` | 站内消息（system / interview / agreement / apply） |
| `dict_value` | 数据字典（岗位类型 / 投递状态 / 企业性质 / 面试 / 协议 / 通知 / 周报状态） |

建库脚本：`backend/sxzp-server/src/main/resources/db/schema.sql`（破坏性重建，含初始化数据）；
旧库升级：`migration_incremental.sql`（幂等增量）；可选的演示数据：`demo_seed_data.sql`、`demo_interview_data.sql`。

---

## 📡 接口概览

统一返回体：`{ "code": 200, "message": "...", "data": ... }`；认证：`Authorization: Bearer <token>`。
后端共 **68 个 REST 接口**，按模块前缀组织：

| 模块 | 前缀 | 数量 | 权限 |
| --- | --- | --- | --- |
| 认证 | `/api/auth` | 4 | 登录/注册匿名，其余登录 |
| 岗位 | `/api/job` | 6 | 检索/详情匿名，发布类企业 |
| 企业 | `/api/company` | 3 | 档案企业，详情匿名 |
| 简历 | `/api/resume` | 5 | 学生 |
| 投递 | `/api/apply` | 5 | 学生 / 企业 |
| 面试 | `/api/interview` | 6 | 学生 / 企业 |
| 协议 | `/api/agreement` | 5 | 学生 / 企业 |
| 周报 | `/api/weekly` | 5 | 学生 / 企业 |
| 消息 | `/api/notice` | 4 | 登录用户 |
| 文件 | `/api/file` | 1 | 登录用户 |
| 字典 | `/api/dict` | 1 | 匿名 |
| 管理端 | `/api/admin/**` | 14 | 管理员 |
| 公开统计 | `/api/public/stats/**` | 8 | 匿名（数据脱敏） |
| 接口文档 | `/api/doc` | 1 | 匿名（启动时自动扫描） |

> 完整接口清单见 [`backend/README.md`](backend/README.md)。

---

## 🚀 快速开始

### 0. 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 8+（推荐 8 / 11 / 17） |
| Maven | 3.6+ |
| MySQL | 8.x |
| Node.js | 16+（推荐 18 LTS） |

### 1. 初始化数据库

```bash
# cmd
mysql -u root -p < "backend\sxzp-server\src\main\resources\db\schema.sql"

# PowerShell（推荐，避免中文路径/编码问题）
Get-Content .\backend\sxzp-server\src\main\resources\db\schema.sql -Raw -Encoding UTF8 |
  mysql -u root -p --default-character-set=utf8mb4
```

### 2. 启动后端

修改 `backend/sxzp-server/src/main/resources/application.yml` 中的数据库账号密码（生产环境同时替换 `sxzp.jwt.secret`），然后：

```bash
cd backend
mvn clean compile
mvn -pl sxzp-server spring-boot:run
```

启动成功：`Tomcat started on port(s): 8080`。
离线环境可加 `-o`（`mvn -o clean compile`），详见后端 README 的离线构建说明。

### 3. 启动前端

```bash
cd frontend/new
npm install
npm run serve
```

访问 **http://localhost:8081**（`/api`、`/uploads` 已代理到 8080，需先启动后端）。

生产构建：`npm run build`，`dist/` 部署到 Nginx 并反代 `/api`、`/uploads`。

### 4. 验证

```bash
# 公开统计接口
curl http://localhost:8080/api/public/stats/dashboard
# 自动生成的接口清单
curl http://localhost:8080/api/doc
```

### 5. 测试账号（口令均为 `123456`）

| 账号 | 角色 | 说明 |
| --- | --- | --- |
| `admin` | 管理员 | 系统管理员 |
| `2021001` | 学生 | 张同学（计算机学院 / 软件工程） |
| `company1` | 企业 | 示例科技有限公司（已认证） |

---

## 🧭 前端页面与路由

| 路径 | 页面 | 权限 |
| --- | --- | --- |
| `/` | 首页（含「数据大屏」入口） | 公开 |
| `/jobs` · `/jobs/:id` · `/companies/:id` | 岗位检索 / 岗位详情 / 企业详情 | 公开 |
| `/login` · `/register` | 登录 / 注册 | 公开 |
| `/screen` | 数据大屏（KPI、趋势、分布、排行、成功率） | 公开 |
| `/student/**` | 我的投递 / 简历 / 面试 / 协议 / 周报 / 消息 | 学生 |
| `/company/**` | 企业档案 / 我的岗位 / 收到的投递 / 面试 / 协议 / 周报 | 企业 |
| `/admin/**` | 仪表盘 / 数据分析 / 企业审核 / 岗位审核 | 管理员 |

> 路由守卫：未登录访问受保护页面跳转 `/login?redirect=...`；角色不符跳转对应角色首页。

---

## 🔐 安全设计

- 口令 **BCrypt** 加密存储；JWT 无状态认证（24 小时有效）
- **三层权限校验**：`SecurityConfig` URL 级放行清单 → 方法级 `@PreAuthorize` 角色控制 → 服务层数据归属校验（防越权）
- 401 / 403 返回真实 HTTP 状态与统一错误体，前端统一跳转登录 / 提示权限不足
- 简历快照、文件上传（扩展名白名单 + 大小限制）等敏感内容可回溯
- 公开大屏接口仅返回聚合统计，学生姓名/学号 **脱敏**（`李**` / `2021***`）

> ⚠️ 部署前务必：① 替换 JWT 密钥；② 修改数据库账号密码；③ 收紧 CORS 白名单；④ 生产环境改用 HTTPS。

---

## 📚 配套文档

| 文档 | 位置 |
| --- | --- |
| 后端详细说明（技术栈 / 68 接口清单 / 建库 / 离线构建） | [`backend/README.md`](backend/README.md) |
| 前端详细说明（目录 / 路由 / 部署 / 常见问题） | [`frontend/new/README.md`](frontend/new/README.md) |
| 后端交接文档（环境、凭据、进度、踩坑记录） | [`HANDOVER.md`](HANDOVER.md) |
| 软件需求规约 / 数据库设计说明书 / 系统构架设计说明书等 | `报告/` 目录（docx） |

---

## ❓ 常见问题

**Q：前端启动后接口 401 / 页面空白？**
先确认后端已在 `8080` 启动；Token 过期时重新登录即可（前端会校验过期 Token 并清理登录态）。

**Q：8081 或 8080 端口被占用？**
修改 `frontend/new/vue.config.js` 的 `devServer.port`，或释放占用 `8080` 的进程。

**Q：数据库没有 `interview` / `agreement` 等表？**
旧库需执行增量脚本 `db/migration_incremental.sql`。

**Q：控制台出现 `ResizeObserver loop completed with undelivered notifications`？**
浏览器无害告警，开发服务器已精确过滤，不影响功能与生产构建。

---

## 🗺️ 后续规划

- [ ] 单元测试与集成测试
- [ ] OpenAPI / Swagger（当前为自研 `/api/doc` 扫描）
- [ ] 文件存储抽象（本地 → OSS / S3）
- [ ] Docker 化部署与 CI

---

## 📄 License

本项目仅用于课程设计 / 教学实训用途。
