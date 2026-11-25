# 校园实习招聘管理平台 · 后端服务

> Campus Internship Recruitment Platform — Backend Service
>
> 基于 Spring Boot 2.7 + MyBatis-Plus + MySQL 8 + Spring Security(JWT) 的实习招聘全流程管理后端，覆盖学生、企业、管理员三类角色。

---

## 📖 项目简介

校园实习招聘管理平台面向高校实习招聘场景，打通「岗位发布 → 审核 → 检索 → 投递 → 面试 → 协议签署 → 周报评价」的完整业务闭环，并沉淀可统计的实习数据。

后端采用 **前后端分离** 架构，对外提供 RESTful 接口（统一返回体 `{ code, message, data }`），认证方式为 **无状态 JWT + BCrypt 口令**，权限模型为 **角色级 `@PreAuthorize` + 服务层数据归属校验** 双层防护。

---

## ✨ 功能特性

| 角色 | 功能 |
| --- | --- |
| 👨‍🎓 学生 | 注册登录、岗位检索与详情、在线简历（多份/默认/公开开关）、简历投递（快照留痕）、投递追踪、面试确认/取消、协议签署/拒绝、提交实习周报、站内消息 |
| 🏢 企业 | 企业注册与资质认证、企业档案维护、岗位发布/修改/下线、申请筛选（通过/淘汰）、安排面试、发起实习协议、周报评分与评语、站内消息 |
| 🛡️ 管理员 | 企业认证审核、岗位合规审核、平台统计（仪表盘/岗位排行/投递趋势/投递成功率等）、接口文档自动生成 |
| 🌐 匿名访客 | 岗位检索、岗位详情、企业详情、公开数据大屏统计接口（学生信息已脱敏） |

**关键业务规则**

- 企业未通过认证不能发布岗位
- 岗位须为 `online`（审核通过）才能被检索与投递
- 同一岗位同一简历仅允许一次有效投递（数据库唯一键 + 服务层双校验）
- 投递时固化简历快照（JSON），保证历史可回溯
- 招满校验、越权防护（学生只能操作自己的简历/投递，企业只能处理自己岗位的投递）
- 企业关键资料变更后需重新认证
- 面试状态机：`pending → confirmed → done / cancelled`；协议状态机：`pending → signed / rejected`；周报状态机：`submitted → reviewed`

---

## 🧱 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 1.8（编译目标） | 兼容 JDK 8 / 11 / 17 运行 |
| Spring Boot | 2.7.18 | Web / Validation / Security |
| MyBatis-Plus | 3.5.3.1 | 纯 `BaseMapper`，无 XML，分页插件 |
| MySQL Connector | 8.0.x（随 Boot 管理） | `com.mysql:mysql-connector-j` |
| JJWT | 0.12.5 | api / impl / jackson |
| Lombok | 1.18.30 | 简化实体 |
| 数据库 | MySQL 8.x | InnoDB，utf8mb4 |

构建工具：Maven 3.6+（多模块，父 POM 统一管理版本）。

---

## 🏗️ 架构与目录

```
backend/
├── pom.xml                        # 父 POM（packaging=pom，统一版本）
└── sxzp-server/                   # 唯一业务模块
    ├── pom.xml
    └── src/main/
        ├── java/com/sxzp/
        │   ├── SxzpApplication.java          # 启动类
        │   ├── common/
        │   │   ├── config/                   # MyBatis-Plus 分页、自动填充、WebMvc(静态资源映射)
        │   │   ├── controller/               # 接口文档自动扫描 /api/doc
        │   │   ├── exception/                # 全局异常处理、BizException
        │   │   ├── result/                   # Result、PageResult 统一返回
        │   │   └── security/                 # JWT 工具/过滤器/安全配置/登录上下文
        │   └── module/                       # 按业务分包（entity/mapper/dto/vo/service/controller）
        │       ├── auth/         登录注册
        │       ├── user/         账号与角色实体
        │       ├── company/      企业档案与认证
        │       ├── student/      学生档案
        │       ├── job/          实习岗位
        │       ├── resume/       学生简历
        │       ├── apply/        简历投递
        │       ├── interview/    面试安排
        │       ├── agreement/    实习协议
        │       ├── weekly/       实习周报与评价
        │       ├── notice/       站内消息
        │       ├── file/         文件上传
        │       ├── dict/         数据字典
        │       ├── stats/        统计报表 / 公开大屏统计
        │       └── admin/        管理端审核接口
        └── resources/
            ├── application.yml               # 数据源 / JWT / 上传 / MyBatis-Plus 配置
            └── db/                           # 建库与演示数据脚本
```

**分层约定**：`Controller → Service → Mapper(MyBatis-Plus) → Entity`；VO 对外输出、DTO 接收入参；统一异常与统一返回体。

---

## 🚀 快速开始

### 1. 环境要求

- JDK 8 及以上（推荐 8 / 11 / 17）
- Maven 3.6+
- MySQL 8.x

### 2. 初始化数据库

```bash
# 方式一（cmd）
mysql -u root -p < "sxzp-server\src\main\resources\db\schema.sql"

# 方式二（PowerShell，避免中文路径/编码问题）
Get-Content .\sxzp-server\src\main\resources\db\schema.sql -Raw -Encoding UTF8 |
  mysql -u root -p --default-character-set=utf8mb4
```

`schema.sql` 会自动 `CREATE DATABASE IF NOT EXISTS sxzp` 并创建 **12 张表** 与初始化数据。

> ⚠️ 该脚本包含 `DROP TABLE IF EXISTS`，属于**破坏性重建**脚本，请勿在生产库直接执行。
>
> 若已有旧版本库（仅 8 张表），请执行增量脚本 `db/migration_incremental.sql`（幂等，不删数据）。

可选演示数据：

- `db/demo_seed_data.sql` — 批量演示数据（投递/面试/协议/周报/站内信）
- `db/demo_interview_data.sql` — 面试演示数据

### 3. 修改配置

编辑 `sxzp-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sxzp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root          # 改成你的 MySQL 账号
    password: 1234          # 改成你的 MySQL 密码

sxzp:
  jwt:
    secret: change-me-to-a-long-random-secret   # 生产环境务必替换
  upload:
    dir: ./uploads          # 上传文件目录（启动目录相对路径）
```

### 4. 构建与运行

```bash
# 编译
mvn clean compile

# 启动（必须指定子模块 -pl sxzp-server）
mvn -pl sxzp-server spring-boot:run
```

或用 IDEA 直接运行 `com.sxzp.SxzpApplication`。

启动成功标志：

```
Tomcat started on port(s): 8080 (http)
Started SxzpApplication in x.xxx seconds
```

> 💡 **离线环境**：本机若无法访问 Maven 中央仓库，可加 `-o` 使用本地仓库离线构建，例如 `mvn -o clean compile`。注意本工程为保证离线可构建，`spring-boot-maven-plugin` 显式指定为 **2.7.6**（parent 为 2.7.18），属已知约束，请勿随意改动。

### 5. 验证

```bash
# 公开统计接口（无需登录）
curl http://localhost:8080/api/public/stats/dashboard

# 自动生成的接口清单（无需登录）
curl http://localhost:8080/api/doc

# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"account\":\"admin\",\"password\":\"123456\"}"
```

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
| `job_apply` | 投递记录（含简历快照 JSON，`uk_apply_job_resume` 防重复投递） |
| `interview` | 面试安排（pending / confirmed / cancelled / done） |
| `agreement` | 实习协议（pending / signed / rejected） |
| `weekly_report` | 实习周报与评价（submitted / reviewed） |
| `notice` | 站内消息（system / interview / agreement / apply） |
| `dict_value` | 数据字典（job_type / apply_state / company_category / interview_state / agreement_state / notice_type / weekly_state） |

**测试账号（口令均为 `123456`）**

| 账号 | 角色 | 说明 |
| --- | --- | --- |
| `admin` | 管理员 | 系统管理员 |
| `2021001` | 学生 | 张同学（计算机学院 / 软件工程） |
| `company1` | 企业 | 示例科技有限公司（已认证） |

---

## 📡 接口一览（共 68 个）

统一返回体：`{ "code": 200, "message": "...", "data": ... }`
认证方式：请求头 `Authorization: Bearer <token>`

### 认证 `/api/auth`（登录/注册匿名）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 JWT |
| POST | `/api/auth/register` | 注册（学生 / 企业；企业注册后为 pending） |
| GET | `/api/auth/me` | 当前登录用户 |
| POST | `/api/auth/logout` | 退出 |

### 岗位 `/api/job`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/job/search` | 匿名 | 关键词/类型/地点/方向/津贴检索 + 排序分页 |
| GET | `/api/job/detail/{jobId}` | 匿名 | 岗位详情（学生视角附是否已投递） |
| POST | `/api/job/save` | 企业 | 发布/修改（submit=false 存草稿） |
| GET | `/api/job/my` | 企业 | 我的岗位（按状态筛选） |
| PUT | `/api/job/offline/{jobId}` | 企业/管理员 | 下线 |
| PUT | `/api/job/resubmit/{jobId}` | 企业 | 草稿/驳回重新提交审核 |

### 企业 `/api/company`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/company/profile` | 企业 | 我的企业档案 |
| PUT | `/api/company/profile` | 企业 | 完善资料（关键信息变更 → 重新待审核） |
| GET | `/api/company/detail/{companyId}` | 匿名 | 企业详情（对外隐藏信用代码） |

### 简历 `/api/resume`（学生）

`GET /list` · `GET /detail/{resumeId}` · `POST /save` · `PUT /default/{resumeId}` · `DELETE /{resumeId}`

### 投递 `/api/apply`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/apply/submit` | 学生 | 投递（生成简历快照，防重复/防招满） |
| PUT | `/api/apply/withdraw/{applyId}` | 学生 | 撤回（仅 pending） |
| GET | `/api/apply/my` | 学生 | 我的投递 |
| GET | `/api/apply/received` | 企业 | 收到的投递（按岗位/状态筛选） |
| POST | `/api/apply/audit` | 企业 | 处理：interview / rejected |

### 面试 `/api/interview`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/interview/schedule` | 企业 | 安排面试 |
| GET | `/api/interview/my` | 学生 | 我的面试 |
| GET | `/api/interview/sent` | 企业 | 我发出的面试 |
| PUT | `/api/interview/confirm/{id}` | 学生 | 确认面试 |
| PUT | `/api/interview/cancel/{id}` | 学生/企业 | 取消面试 |
| PUT | `/api/interview/done/{id}` | 企业 | 标记完成 |

### 协议 `/api/agreement`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/agreement/save` | 企业 | 发起实习协议 |
| GET | `/api/agreement/my` | 学生 | 我的协议 |
| GET | `/api/agreement/sent` | 企业 | 我发出的协议 |
| PUT | `/api/agreement/sign/{id}` | 学生 | 签署 |
| PUT | `/api/agreement/reject/{id}` | 学生 | 拒绝 |

### 周报 `/api/weekly`

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/weekly/submit` | 学生 | 提交周报 |
| GET | `/api/weekly/my` | 学生 | 我的周报 |
| GET | `/api/weekly/received` | 企业 | 收到的周报 |
| GET | `/api/weekly/detail/{reportId}` | 登录 | 周报详情（服务层归属校验） |
| POST | `/api/weekly/review` | 企业 | 评价周报（评分 1-5 + 评语） |

### 消息 `/api/notice`（登录）

`GET /list` · `GET /unread-count` · `PUT /read/{noticeId}` · `PUT /read-all`

### 文件 / 字典 / 文档

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/file/upload` | 登录 | 上传（白名单扩展名，`./uploads/yyyy-MM-dd/UUID.ext`） |
| GET | `/api/dict/{typeCode}` | 匿名 | 字典取值（下拉框枚举） |
| GET | `/api/doc` | 匿名 | 自动扫描生成的接口清单 JSON |

### 管理端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/company/pending` | 待认证企业列表 |
| GET | `/api/admin/company/page` | 企业分页查询 |
| POST | `/api/admin/company/audit` | 企业认证审核 |
| GET | `/api/admin/job/pending` | 待审核岗位列表 |
| GET | `/api/admin/job/page` | 岗位分页查询 |
| POST | `/api/admin/job/audit` | 岗位审核（通过后置 publish_time） |
| GET | `/api/admin/stats/dashboard` | 仪表盘总览 |
| GET | `/api/admin/stats/job-ranking` | 热门岗位排行 |
| GET | `/api/admin/stats/apply-trend` | 投递趋势 |
| GET | `/api/admin/stats/job-type-distribution` | 岗位类型分布 |
| GET | `/api/admin/stats/college-distribution` | 学生学院分布 |
| GET | `/api/admin/stats/company-auth-distribution` | 企业认证状态分布 |
| GET | `/api/admin/stats/apply-success` | 投递成功率（整体 + 按岗位类型） |
| GET | `/api/admin/stats/recent-applies` | 最近投递动态 |

### 公开统计（数据大屏，匿名）

`GET /api/public/stats/{dashboard | apply-trend | job-ranking | job-type-distribution | college-distribution | company-auth-distribution | apply-success | recent-applies}`

> 公开接口仅暴露聚合统计；最近投递中的学生姓名/学号已脱敏（如 `李**`、`2021***`）。

---

## 📁 静态资源与上传

- 上传文件保存至 `sxzp.upload.dir`（默认 `./uploads`，按日期分目录、UUID 命名）
- 通过 `WebMvcConfig` 将 `/uploads/**` 映射到本地目录，匿名可访问
- 允许扩展名：`jpg jpeg png gif pdf doc docx xls xlsx zip rar`
- 单文件 10MB / 请求 20MB（`spring.servlet.multipart`）

---

## 🔐 安全说明

- 口令使用 BCrypt 加密存储
- JWT 无状态认证，令牌 24 小时有效（`sxzp.jwt.expire`）
- URL 级鉴权（`SecurityConfig`）+ 方法级 `@PreAuthorize` + 服务层数据归属校验三层防护
- 401/403 返回真实 HTTP 状态与统一错误体，前端据此统一跳转登录

> ⚠️ 部署前请务必：① 替换 `sxzp.jwt.secret`；② 修改数据库账号密码；③ 生产环境按需收紧 CORS 白名单；④ 敏感附件建议改用鉴权访问或对象存储。

---

## 🗺️ 后续规划

- [ ] 引入单元测试与集成测试
- [ ] 引入 OpenAPI/Swagger（当前为自研 `/api/doc` 扫描）
- [ ] 部署脚本与 Dockerfile
- [ ] 文件存储抽象（本地 → OSS/S3）

---

## 📄 License

本项目仅用于课程设计 / 教学实训用途。
