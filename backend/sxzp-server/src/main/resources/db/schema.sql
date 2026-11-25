-- =============================================================
-- 校园实习招聘管理平台 数据库脚本 (MySQL 8.x)
-- 依据《校园实习招聘管理平台_数据库设计说明书》
-- 本次实现范围：登录认证 + 企业 + 岗位 + 简历 + 投递（核心闭环）
-- =============================================================
CREATE DATABASE IF NOT EXISTS sxzp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE sxzp;

-- ---------- 用户与角色 ----------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  user_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  account      VARCHAR(40)  NOT NULL COMMENT '登录账号(学号/工号/企业账号)',
  password     VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
  user_type    VARCHAR(16)  NOT NULL COMMENT 'student/company/admin',
  status       VARCHAR(16)  NOT NULL DEFAULT 'normal' COMMENT 'normal/disabled',
  real_name    VARCHAR(40)      NULL COMMENT '姓名',
  email        VARCHAR(80)      NULL COMMENT '邮箱',
  phone        VARCHAR(20)      NULL COMMENT '手机号',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_account (account),
  KEY idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统统一账号表';

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  role_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_code   VARCHAR(24) NOT NULL COMMENT '系统角色代码',
  role_name   VARCHAR(40) NOT NULL COMMENT '角色显示名',
  permissions VARCHAR(255)    NULL COMMENT '逗号分隔的权限标识',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色定义(RBAC)';

-- ---------- 企业 ----------
DROP TABLE IF EXISTS company;
CREATE TABLE company (
  company_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  user_owner    BIGINT UNSIGNED NOT NULL COMMENT '绑定 sys_user.user_id',
  ent_name      VARCHAR(120) NOT NULL COMMENT '单位名称',
  credit_code   VARCHAR(40)      NULL COMMENT '统一社会信用代码',
  category      VARCHAR(24)      NULL COMMENT '单位性质(字典)',
  contact_name  VARCHAR(40)      NULL COMMENT '联系人',
  contact_phone VARCHAR(20)      NULL COMMENT '联系电话',
  contact_email VARCHAR(80)      NULL COMMENT '联系邮箱',
  address       VARCHAR(160)     NULL COMMENT '单位地址',
  intro         VARCHAR(500)     NULL COMMENT '单位简介',
  cert_file     VARCHAR(255)     NULL COMMENT '资质附件路径',
  auth_state    VARCHAR(16)  NOT NULL DEFAULT 'pending' COMMENT 'pending/auth/reject/freeze',
  reject_reason VARCHAR(255)     NULL COMMENT '驳回意见',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id),
  UNIQUE KEY uk_company_name (ent_name),
  KEY idx_company_owner (user_owner),
  KEY idx_company_state (auth_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业档案与认证';

-- ---------- 学生 ----------
DROP TABLE IF EXISTS student;
CREATE TABLE student (
  student_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  user_owner  BIGINT UNSIGNED NOT NULL COMMENT '绑定 sys_user.user_id',
  real_name   VARCHAR(40) NOT NULL COMMENT '姓名',
  student_no  VARCHAR(24) NOT NULL COMMENT '学号',
  college     VARCHAR(60)     NULL COMMENT '学院',
  major       VARCHAR(60)     NULL COMMENT '专业',
  grade_year  CHAR(4)         NULL COMMENT '入学年',
  edu_expect  DATE            NULL COMMENT '预计毕业时间',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (student_id),
  UNIQUE KEY uk_student_no (student_no),
  KEY idx_student_owner (user_owner)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案';

-- ---------- 岗位 ----------
DROP TABLE IF EXISTS job;
CREATE TABLE job (
  job_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  company_id    BIGINT UNSIGNED NOT NULL COMMENT '企业',
  job_title     VARCHAR(120) NOT NULL COMMENT '岗位名称',
  job_type      VARCHAR(24)      NULL COMMENT '实习类型',
  headcount     INT              NULL COMMENT '拟招人数',
  work_place    VARCHAR(120)     NULL COMMENT '工作地点',
  category      VARCHAR(40)      NULL COMMENT '岗位方向',
  req           TEXT             NULL COMMENT '任职要求',
  duty          TEXT             NULL COMMENT '岗位职责',
  benefit_money DECIMAL(10,2)    NULL COMMENT '实习津贴',
  start_date    DATE             NULL COMMENT '实习开始',
  end_date      DATE             NULL COMMENT '实习结束',
  audit_state   VARCHAR(16)  NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/online/reject/ended',
  audit_opinion VARCHAR(255)     NULL COMMENT '审核意见',
  publish_time  DATETIME         NULL COMMENT '上线时间',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (job_id),
  KEY idx_job_company (company_id),
  KEY idx_job_type (job_type),
  KEY idx_job_state (audit_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实习岗位';

-- ---------- 简历 ----------
DROP TABLE IF EXISTS student_resume;
CREATE TABLE student_resume (
  resume_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  student_id  BIGINT UNSIGNED NOT NULL COMMENT '学生',
  title       VARCHAR(80)      NULL COMMENT '简历名称',
  is_public   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可被企业浏览',
  is_default  TINYINT      NOT NULL DEFAULT 0 COMMENT '默认投递简历',
  edu_json    TEXT             NULL COMMENT '教育经历(JSON)',
  skill       VARCHAR(500)     NULL COMMENT '技能与证书',
  project_exp TEXT             NULL COMMENT '项目/实践经历',
  self_eval   VARCHAR(500)     NULL COMMENT '自我评价',
  cont        TEXT             NULL COMMENT '完整简历正文',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (resume_id),
  KEY idx_resume_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生简历';

-- ---------- 投递 ----------
DROP TABLE IF EXISTS job_apply;
CREATE TABLE job_apply (
  apply_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  job_id          BIGINT UNSIGNED NOT NULL COMMENT '岗位',
  student_id      BIGINT UNSIGNED NOT NULL COMMENT '学生',
  resume_id       BIGINT UNSIGNED NOT NULL COMMENT '简历',
  resume_snapshot TEXT             NULL COMMENT '投递时的简历快照',
  apply_state     VARCHAR(16)  NOT NULL DEFAULT 'pending' COMMENT 'pending/interview/agreement/hired/rejected/withdrawn',
  apply_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (apply_id),
  UNIQUE KEY uk_apply_job_resume (job_id, resume_id),
  KEY idx_apply_job (job_id),
  KEY idx_apply_student (student_id),
  KEY idx_apply_state (apply_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投递记录';

-- ---------- 面试安排 ----------
DROP TABLE IF EXISTS interview;
CREATE TABLE interview (
  interview_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  apply_id       BIGINT UNSIGNED NOT NULL COMMENT '关联投递记录',
  job_id         BIGINT UNSIGNED NOT NULL COMMENT '岗位',
  student_id     BIGINT UNSIGNED NOT NULL COMMENT '学生',
  company_id     BIGINT UNSIGNED NOT NULL COMMENT '企业',
  interview_time DATETIME         NULL COMMENT '面试时间',
  place          VARCHAR(160)     NULL COMMENT '面试地点',
  contact        VARCHAR(40)      NULL COMMENT '联系人',
  contact_phone  VARCHAR(20)      NULL COMMENT '联系电话',
  remark         VARCHAR(500)     NULL COMMENT '备注',
  state          VARCHAR(16)  NOT NULL DEFAULT 'pending' COMMENT 'pending/confirmed/cancelled/done',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (interview_id),
  KEY idx_interview_apply (apply_id),
  KEY idx_interview_student (student_id),
  KEY idx_interview_company (company_id),
  KEY idx_interview_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试安排';

-- ---------- 实习协议 ----------
DROP TABLE IF EXISTS agreement;
CREATE TABLE agreement (
  agreement_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '协议ID',
  apply_id     BIGINT UNSIGNED NOT NULL COMMENT '关联投递记录',
  job_id       BIGINT UNSIGNED NOT NULL COMMENT '岗位',
  student_id   BIGINT UNSIGNED NOT NULL COMMENT '学生',
  company_id   BIGINT UNSIGNED NOT NULL COMMENT '企业',
  start_date   DATE             NULL COMMENT '实习开始日期',
  end_date     DATE             NULL COMMENT '实习结束日期',
  salary       DECIMAL(10,2)    NULL COMMENT '实习津贴',
  content      TEXT             NULL COMMENT '协议内容',
  state        VARCHAR(16)  NOT NULL DEFAULT 'pending' COMMENT 'pending/signed/rejected',
  sign_time    DATETIME         NULL COMMENT '签署时间',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (agreement_id),
  KEY idx_agreement_apply (apply_id),
  KEY idx_agreement_student (student_id),
  KEY idx_agreement_company (company_id),
  KEY idx_agreement_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实习协议';

-- ---------- 消息通知 ----------
DROP TABLE IF EXISTS notice;
CREATE TABLE notice (
  notice_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '接收用户 sys_user.user_id',
  title       VARCHAR(120) NOT NULL COMMENT '标题',
  content     VARCHAR(500)     NULL COMMENT '内容',
  type        VARCHAR(24)  NOT NULL DEFAULT 'system' COMMENT 'system/interview/agreement/apply',
  is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notice_id),
  KEY idx_notice_user (user_id),
  KEY idx_notice_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

-- ---------- 实习周报 ----------
DROP TABLE IF EXISTS weekly_report;
CREATE TABLE weekly_report (
  report_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '周报ID',
  apply_id       BIGINT UNSIGNED NOT NULL COMMENT '关联投递记录',
  job_id         BIGINT UNSIGNED NOT NULL COMMENT '岗位',
  student_id     BIGINT UNSIGNED NOT NULL COMMENT '学生',
  company_id     BIGINT UNSIGNED NOT NULL COMMENT '企业',
  week_num       INT          NOT NULL COMMENT '第几周',
  start_date     DATE             NULL COMMENT '本周开始',
  end_date       DATE             NULL COMMENT '本周结束',
  content        TEXT             NULL COMMENT '本周工作内容',
  plan           TEXT             NULL COMMENT '下周计划',
  problem        TEXT             NULL COMMENT '遇到的问题',
  review_score   INT              NULL COMMENT '企业评分1-5',
  review_comment VARCHAR(500)     NULL COMMENT '企业评语',
  state          VARCHAR(16)  NOT NULL DEFAULT 'submitted' COMMENT 'submitted/reviewed',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id),
  KEY idx_weekly_apply (apply_id),
  KEY idx_weekly_student (student_id),
  KEY idx_weekly_company (company_id),
  KEY idx_weekly_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实习周报';

-- ---------- 字典 ----------
DROP TABLE IF EXISTS dict_value;
CREATE TABLE dict_value (
  dict_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '取值ID',
  type_code  VARCHAR(32) NOT NULL COMMENT '所属字典',
  dict_code  VARCHAR(32) NOT NULL COMMENT '取值代码',
  dict_label VARCHAR(60) NOT NULL COMMENT '显示值',
  sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
  is_enabled TINYINT     NOT NULL DEFAULT 1 COMMENT '可用',
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_dict (type_code, dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典取值';

-- =============================================================
-- 初始化数据（账号口令均为 BCrypt 加密后的 "123456"）
-- =============================================================
INSERT INTO sys_role (role_code, role_name, permissions) VALUES
 ('student','学生','job:view,resume:edit,apply:submit'),
 ('company','企业用户','job:publish,resume:view,apply:audit'),
 ('admin','管理员','company:audit,job:audit,user:manage,stat:view');

INSERT INTO sys_user (account, password, user_type, real_name, email) VALUES
 ('admin',  '$2a$10$9lFqnRKd3r4Q5wAs2.8DBOvA5oOrAIajJhXgGUAkzh212TsjRHZeO', 'admin',   '系统管理员', 'admin@sxzp.edu.cn'),
 ('2021001','$2a$10$9lFqnRKd3r4Q5wAs2.8DBOvA5oOrAIajJhXgGUAkzh212TsjRHZeO', 'student', '张同学',    'stu1@example.com'),
 ('company1','$2a$10$9lFqnRKd3r4Q5wAs2.8DBOvA5oOrAIajJhXgGUAkzh212TsjRHZeO','company','示例企业HR', 'hr@demo.com');

INSERT INTO student (user_owner, real_name, student_no, college, major, grade_year)
SELECT user_id, '张同学', '2021001', '计算机学院', '软件工程', '2021' FROM sys_user WHERE account='2021001';

INSERT INTO company (user_owner, ent_name, credit_code, category, contact_name, contact_phone, contact_email, auth_state)
SELECT user_id, '示例科技有限公司', '91330100MA2XXXXXXX', '民营企业', '示例企业HR', '13800000000', 'hr@demo.com', 'auth'
FROM sys_user WHERE account='company1';

INSERT INTO dict_value (type_code, dict_code, dict_label, sort) VALUES
 ('job_type','teaching','教学实习',1),
 ('job_type','professional','专业实习',2),
 ('job_type','graduation','毕业实习',3),
 ('apply_state','pending','待处理',1),
 ('apply_state','interview','待面试',2),
 ('apply_state','agreement','待协议',3),
 ('apply_state','hired','已录用',4),
 ('apply_state','rejected','已淘汰',5),
 ('apply_state','withdrawn','已撤回',6),
 ('company_category','state','国有企业',1),
 ('company_category','private','民营企业',2),
 ('company_category','foreign','外资企业',3),
 ('company_category','institution','事业单位',4),
 ('interview_state','pending','待确认',1),
 ('interview_state','confirmed','已确认',2),
 ('interview_state','cancelled','已取消',3),
 ('interview_state','done','已完成',4),
 ('agreement_state','pending','待签署',1),
 ('agreement_state','signed','已签署',2),
 ('agreement_state','rejected','已拒绝',3),
 ('notice_type','system','系统通知',1),
 ('notice_type','interview','面试通知',2),
 ('notice_type','agreement','协议通知',3),
 ('notice_type','apply','投递通知',4),
 ('weekly_state','submitted','已提交',1),
 ('weekly_state','reviewed','已评价',2);
