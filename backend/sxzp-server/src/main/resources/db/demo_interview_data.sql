-- =============================================================
-- 演示数据：为演示学生（学号 2021001）的既有投递补充面试记录
-- 幂等脚本：已存在的同状态面试不会重复插入，可安全重跑
-- 在 MySQL sxzp 库中执行
-- =============================================================
USE sxzp;

-- 1. 待确认面试（未来时间，用于体验「确认 / 取消」流程）
INSERT INTO interview
  (apply_id, job_id, student_id, company_id, interview_time, place, contact, contact_phone, remark, state, create_time, update_time)
SELECT ja.apply_id, ja.job_id, ja.student_id, j.company_id,
       DATE_ADD(NOW(), INTERVAL 1 DAY),
       '示例科技大厦 3 楼 301 会议室',
       '示例企业HR',
       '13800000000',
       '请提前 10 分钟到场，携带学生证与简历。',
       'pending',
       NOW(), NOW()
FROM job_apply ja
JOIN job j     ON j.job_id = ja.job_id
JOIN student s ON s.student_id = ja.student_id
JOIN sys_user u ON u.user_id = s.user_owner AND u.account = '2021001'
WHERE NOT EXISTS (
    SELECT 1 FROM interview iv
    WHERE iv.apply_id = ja.apply_id AND iv.state = 'pending'
);

-- 2. 已完成的面试（过去时间，用于展示「已完成」状态）
INSERT INTO interview
  (apply_id, job_id, student_id, company_id, interview_time, place, contact, contact_phone, remark, state, create_time, update_time)
SELECT ja.apply_id, ja.job_id, ja.student_id, j.company_id,
       DATE_SUB(NOW(), INTERVAL 7 DAY),
       '线上会议（腾讯会议）',
       '示例企业HR',
       '13800000000',
       '面试已完成，结果另行通知。',
       'done',
       DATE_SUB(NOW(), INTERVAL 8 DAY),
       DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM job_apply ja
JOIN job j     ON j.job_id = ja.job_id
JOIN student s ON s.student_id = ja.student_id
JOIN sys_user u ON u.user_id = s.user_owner AND u.account = '2021001'
WHERE NOT EXISTS (
    SELECT 1 FROM interview iv
    WHERE iv.apply_id = ja.apply_id AND iv.state = 'done'
);

-- 3. 投递状态与面试记录保持一致（有未完成面试的投递置为 interview）
UPDATE job_apply ja
JOIN student s ON s.student_id = ja.student_id
JOIN sys_user u ON u.user_id = s.user_owner AND u.account = '2021001'
SET ja.apply_state = 'interview'
WHERE ja.apply_state IN ('pending', 'interview')
  AND EXISTS (
    SELECT 1 FROM interview iv
    WHERE iv.apply_id = ja.apply_id AND iv.state IN ('pending', 'confirmed')
  );
