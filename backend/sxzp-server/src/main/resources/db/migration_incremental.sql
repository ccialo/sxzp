-- =============================================================
-- 增量建表脚本（不会删除已有数据，可安全重跑）
-- 在 MySQL sxzp 库中执行
-- =============================================================

USE sxzp;

-- ---------- 面试安排 ----------
CREATE TABLE IF NOT EXISTS interview (
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
CREATE TABLE IF NOT EXISTS agreement (
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
CREATE TABLE IF NOT EXISTS notice (
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
CREATE TABLE IF NOT EXISTS weekly_report (
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

-- =============================================================
-- 字典数据增量（先删后插，避免重复）
-- =============================================================

DELETE FROM dict_value WHERE type_code IN ('interview_state','agreement_state','notice_type','weekly_state');

INSERT INTO dict_value (type_code, dict_code, dict_label, sort) VALUES
 -- 面试状态
 ('interview_state','pending','待确认',1),
 ('interview_state','confirmed','已确认',2),
 ('interview_state','cancelled','已取消',3),
 ('interview_state','done','已完成',4),
 -- 协议状态
 ('agreement_state','pending','待签署',1),
 ('agreement_state','signed','已签署',2),
 ('agreement_state','rejected','已拒绝',3),
 -- 通知类型
 ('notice_type','system','系统通知',1),
 ('notice_type','interview','面试通知',2),
 ('notice_type','agreement','协议通知',3),
 ('notice_type','apply','投递通知',4),
 -- 周报状态
 ('weekly_state','submitted','已提交',1),
 ('weekly_state','reviewed','已评价',2);
