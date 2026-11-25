# 校园实习招聘管理平台 · 前端

> Campus Internship Recruitment Platform — Frontend
>
> 基于 Vue 3 + Element Plus + Pinia + ECharts 的校园实习招聘管理平台前端（学生 / 企业 / 管理员三端 + 公开数据大屏）。

---

## 📖 项目简介

前端采用 **前后端分离** 的单页应用（SPA）架构，按角色划分为 **学生端、企业端、管理端** 三大门户，同时提供匿名可访问的 **岗位检索、岗位详情、企业详情** 与 **公开数据大屏**。

开发与联调通过 Vue CLI 开发服务器代理（`/api`、`/uploads` → 后端 `8080`）直连 Spring Boot 后端，无需 Mock 层。

---

## ✨ 功能概览

| 端 | 页面 |
| --- | --- |
| 🌐 公共 | 首页、岗位检索（多条件筛选 + 分页）、岗位详情、企业详情、登录、注册 |
| 👨‍🎓 学生 | 我的投递、我的简历 / 简历编辑、我的面试、我的协议、我的周报 / 提交周报、消息通知 |
| 🏢 企业 | 企业档案（资质上传）、我的岗位 / 发布岗位、收到的投递（查看简历快照、安排面试、淘汰）、已发出面试、已发出协议、收到的周报（评分）、消息通知 |
| 🛡️ 管理员 | 仪表盘、数据分析（趋势/分布/排行/成功率）、企业审核、岗位审核 |
| 🖥️ 数据大屏 | `/screen` 全屏可视化大屏（匿名可访问）：KPI、投递趋势、状态分布、热门岗位、学院分布、投递成功率、最近动态 |

其他特性：

- JWT 登录态持久化、路由守卫按角色拦截、菜单按角色动态渲染
- 统一请求封装：自动注入 `Bearer` Token、统一解析 `{code, message, data}`、401/403 统一处理
- 字典缓存（`useDict` / `DictTag`）、分页复用（`usePagination`）、状态徽章（`StateBadge`）
- 文件上传 / 预览组件、简历快照表格化预览、图表封装（`ChartBase`，基于 ECharts）

---

## 🧱 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Vue | ^3.2（`<script setup>`） | 组合式 API |
| Vue Router | ^4.2 | 路由与守卫 |
| Pinia | ^2.1 | 状态管理 |
| Axios | ^1.6 | HTTP 请求 |
| Element Plus | ^2.4 | UI 组件库 |
| ECharts | ^5.4 | 数据可视化 |
| dayjs | ^1.11 | 日期处理 |
| Vue CLI | ~5.0（Webpack） | 构建与开发服务器 |
| Sass | ^1.69 | 样式预处理 |

---

## 🏗️ 目录结构

```
new/
├── public/                     # 静态资源
├── src/
│   ├── api/                    # 接口封装（按模块拆分 + request.js 统一封装）
│   ├── assets/                 # 样式与静态资源
│   ├── components/             # 通用组件（JobCard/DictTag/StateBadge/ChartBase/FileUpload…）
│   ├── composables/            # 组合式函数（useDict / usePagination）
│   ├── layouts/                # 布局（PublicLayout / BasicLayout / BlankLayout）
│   ├── router/                 # 路由（index + modules/public|student|company|admin）
│   ├── stores/                 # Pinia（auth / dict / user）
│   ├── utils/                  # 工具（enums、menus、format、auth、message）
│   ├── views/
│   │   ├── Home.vue            # 首页（含「数据大屏」入口）
│   │   ├── DataScreen.vue      # 公开数据大屏
│   │   ├── public/             # 登录/注册/岗位检索/岗位详情/企业详情
│   │   ├── student/            # 学生端页面
│   │   ├── company/            # 企业端页面
│   │   ├── admin/              # 管理端页面
│   │   └── error/              # 403 / 404
│   ├── permission.js           # 全局路由守卫
│   ├── main.js                 # 入口
│   └── App.vue
├── vue.config.js               # dev 端口 8081、/api 与 /uploads 代理、@ 别名
├── package.json
└── .gitignore
```

---

## 🚀 快速开始

### 1. 环境要求

- Node.js **16+**（推荐 18 LTS）
- npm 8+
- 后端服务运行在 `http://localhost:8080`（接口与文件上传均经代理）

### 2. 安装依赖

```bash
npm install
```

### 3. 启动开发服务器

```bash
npm run serve
```

访问：**http://localhost:8081**

开发服务器已配置代理（`vue.config.js`）：

| 前缀 | 目标 |
| --- | --- |
| `/api` | `http://localhost:8080` |
| `/uploads` | `http://localhost:8080` |

> ⚠️ 请先启动后端服务，否则登录/数据请求会失败（返回 401 或连接被拒）。

### 4. 生产构建

```bash
npm run build
```

构建产物输出至 `dist/`，可部署到 Nginx 等静态服务器，并将 `/api`、`/uploads` 反向代理到后端。

**Nginx 参考配置**

```nginx
server {
    listen 80;
    server_name your-domain;

    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;   # SPA history 路由回退
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }

    location /uploads {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }
}
```

### 5. 代码检查

```bash
npm run lint
```

---

## 🧭 路由一览

| 路径 | 页面 | 权限 |
| --- | --- | --- |
| `/` | 首页 | 公开 |
| `/jobs` `/jobs/:id` `/companies/:id` | 岗位检索 / 岗位详情 / 企业详情 | 公开 |
| `/login` `/register` | 登录 / 注册 | 公开 |
| `/screen` | 数据大屏 | 公开 |
| `/student/applies` | 我的投递 | 学生 |
| `/student/resumes` `/student/resumes/edit` | 我的简历 / 简历编辑 | 学生 |
| `/student/interviews` | 我的面试 | 学生 |
| `/student/agreements` | 我的协议 | 学生 |
| `/student/weekly` `/student/weekly/submit` | 我的周报 / 提交周报 | 学生 |
| `/student/notices` | 消息通知 | 学生 |
| `/company/profile` | 企业档案 | 企业 |
| `/company/jobs` `/company/jobs/edit` | 我的岗位 / 发布修改岗位 | 企业 |
| `/company/applies` | 收到的投递 | 企业 |
| `/company/interviews` | 已发出面试 | 企业 |
| `/company/agreements` | 已发出协议 | 企业 |
| `/company/weekly` | 收到的周报 | 企业 |
| `/company/notices` | 消息通知 | 企业 |
| `/admin/dashboard` | 仪表盘 | 管理员 |
| `/admin/analysis` | 数据分析 | 管理员 |
| `/admin/companies` | 企业审核 | 管理员 |
| `/admin/jobs` | 岗位审核 | 管理员 |

> 路由守卫：未登录访问 `requireAuth` 路由会跳转 `/login?redirect=...`；角色不符跳转对应角色首页。

---

## 🔌 与后端的约定

**统一返回体**

```json
{ "code": 200, "message": "操作成功", "data": { } }
```

- `request.js` 在 `code === 200` 时直接返回 `data`，否则统一弹出错误提示
- HTTP 401 → 清除登录态并提示重新登录；HTTP 403 → 提示权限不足
- 分页数据结构：`{ records, total, current, size }`

**认证**

```
Authorization: Bearer <token>
```

Token 与用户信息持久化在 `localStorage`（key：`sxzp_token` / `sxzp_user`），页面刷新后通过 `GET /api/auth/me` 恢复。

**状态字典（与后端一致）**

| 字典 | 取值 |
| --- | --- |
| 岗位审核状态 `job.audit_state` | `draft` / `pending` / `online` / `reject` / `ended` |
| 投递状态 `job_apply.apply_state` | `pending` / `interview` / `agreement` / `hired` / `rejected` / `withdrawn` |
| 面试状态 `interview.state` | `pending` / `confirmed` / `cancelled` / `done` |
| 协议状态 `agreement.state` | `pending` / `signed` / `rejected` |
| 周报状态 `weekly_report.state` | `submitted` / `reviewed` |
| 企业认证状态 `company.auth_state` | `pending` / `auth` / `reject` / `freeze` |
| 通知类型 `notice.type` | `system` / `interview` / `agreement` / `apply` |

以上枚举通过 `GET /api/dict/{typeCode}` 获取（`useDict` 已做缓存与并发去重）。

---

## ❓ 常见问题

**1. 启动后页面空白 / 接口 401**
请确认后端已在 `8080` 端口启动；若刚登录过而 Token 已过期，重新登录即可（路由守卫会校验过期 Token）。

**2. 8081 端口被占用**
修改 `vue.config.js` 中 `devServer.port`，或先结束占用进程。

**3. 控制台出现 `ResizeObserver loop completed with undelivered notifications`**
浏览器对观察者回调的无害告警。`vue.config.js` 已通过 `devServer.client.overlay.runtimeErrors` 精确过滤该提示（仅开发环境浮层，不影响功能与生产构建）。

**4. 上传的图片 / 附件打不开**
确认后端 `sxzp.upload.dir` 目录与 `/uploads/**` 静态映射正常，且 Nginx/代理已转发 `/uploads`。

**5. 构建体积较大**
`echarts` 与 `element-plus` 体积较大属正常；如需优化可开启按需引入（unplugin-vue-components）与 `productionSourceMap: false`。

---

## 📄 License

本项目仅用于课程设计 / 教学实训用途。
