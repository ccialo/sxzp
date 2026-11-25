-- =============================================================
-- 演示数据：批量生成用户/企业/岗位
--   * 企业账号 50 个（company1 已有，新增 company2..company50，密码均为 123456）
--   * 学生账号 50 个（2021001 已有，新增 2022xxxx 共 49 个，密码均为 123456）
--   * 企业档案 50 家、学生档案 50 份、岗位 150 个
-- 幂等：依赖账号/企业名/学号唯一键去重，岗位总量不足 150 时补齐 150 条，可安全重跑
-- 在 MySQL sxzp 库中执行
-- =============================================================
USE sxzp;

SET NAMES utf8mb4;

-- 统一密码：BCrypt("123456")，与 schema.sql 种子一致
-- ---------- 1. 企业用户（company2..company50） ----------
INSERT INTO sys_user (account, password, user_type, status, real_name, email, phone, create_time, update_time)
WITH RECURSIVE seq AS (
  SELECT 2 AS n
  UNION ALL SELECT n + 1 FROM seq WHERE n < 50
)
SELECT CONCAT('company', n),
       '$2a$10$9lFqnRKd3r4Q5wAs2.8DBOvA5oOrAIajJhXgGUAkzh212TsjRHZeO',
       'company', 'normal',
       CONCAT(ELT(1 + (n * 7) MOD 20, '王','李','张','刘','陈','杨','赵','黄','周','吴','徐','孙','马','朱','胡','郭','何','高','林','罗'),
              ELT(1 + (n * 3) MOD 20, '伟','芳','娜','洋','静','帆','磊','敏','杰','婷','强','丽','军','琳','翔','悦','平','雪','超','丹')),
       CONCAT('hr', n, '@demo', n, '.com'),
       CONCAT('139', LPAD(10000000 + n * 137, 8, '0')),
       NOW(), NOW()
FROM seq
WHERE NOT EXISTS (SELECT 1 FROM sys_user u WHERE u.account = CONCAT('company', n));

-- ---------- 2. 学生用户（20220001..20220049） ----------
INSERT INTO sys_user (account, password, user_type, status, real_name, email, phone, create_time, update_time)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL SELECT n + 1 FROM seq WHERE n < 49
)
SELECT CONCAT('2022', LPAD(n, 4, '0')),
       '$2a$10$9lFqnRKd3r4Q5wAs2.8DBOvA5oOrAIajJhXgGUAkzh212TsjRHZeO',
       'student', 'normal',
       CONCAT(ELT(1 + (n * 11) MOD 20, '王','李','张','刘','陈','杨','赵','黄','周','吴','徐','孙','马','朱','胡','郭','何','高','林','罗'),
              ELT(1 + (n * 5) MOD 20, '伟','芳','娜','洋','静','帆','磊','敏','杰','婷','强','丽','军','琳','翔','悦','平','雪','超','丹')),
       CONCAT('stu', n, '@stu.sxzp.edu.cn'),
       CONCAT('135', LPAD(20000000 + n * 211, 8, '0')),
       NOW(), NOW()
FROM seq
WHERE NOT EXISTS (SELECT 1 FROM sys_user u WHERE u.account = CONCAT('2022', LPAD(n, 4, '0')));

-- ---------- 3. 企业档案（company2..company50，共 49 家；company1 已有） ----------
INSERT INTO company (user_owner, ent_name, credit_code, category, contact_name, contact_phone, contact_email, address, intro, cert_file, auth_state, reject_reason, create_time, update_time)
WITH RECURSIVE seq AS (
  SELECT 2 AS n
  UNION ALL SELECT n + 1 FROM seq WHERE n < 50
)
SELECT u.user_id,
       CONCAT(ELT(1 + n MOD 10, '杭州','北京','上海','深圳','广州','成都','武汉','南京','西安','苏州'),
              ELT(1 + (n + n DIV 10) MOD 10, '云启','华信','中科','蓝海','星河','瑞驰','天辰','鼎盛','博远','恒达'),
              ELT(1 + (n + 2 * (n DIV 10)) MOD 10, '科技','信息','网络','软件','电子','生物','教育','传媒','金融','智能'),
              '有限公司'),
       CONCAT('91', LPAD(MOD(300000000 + n * 7919, 100000000), 8, '0'), LPAD(n * 331, 8, '0')),
       ELT(1 + n MOD 4, 'private', 'state', 'foreign', 'institution'),
       CONCAT(ELT(1 + (n * 3) MOD 20, '王','李','张','刘','陈','杨','赵','黄','周','吴','徐','孙','马','朱','胡','郭','何','高','林','罗'),
              ELT(1 + (n * 7) MOD 20, '伟','芳','娜','洋','静','帆','磊','敏','杰','婷','强','丽','军','琳','翔','悦','平','雪','超','丹')),
       CONCAT('138', LPAD(30000000 + n * 331, 8, '0')),
       CONCAT('hr', n, '@', ELT(1 + n MOD 10, 'hangzhou','beijing','shanghai','shenzhen','guangzhou','chengdu','wuhan','nanjing','xian','suzhou'), '-demo.com'),
       CONCAT(ELT(1 + n MOD 10, '杭州','北京','上海','深圳','广州','成都','武汉','南京','西安','苏州'),
              '市', ELT(1 + n MOD 8, '滨江','朝阳','浦东','南山','天河','高新','光谷','玄武'), '区',
              ELT(1 + n MOD 8, '科技','创新','软件','智谷','商务','数码','文一','中山'), '路 ', 100 + n, '号'),
       CONCAT('专注于', ELT(1 + n MOD 10, '企业级软件开发','人工智能应用','大数据分析','云计算服务','物联网','智能制造','在线教育','数字传媒','金融科技','智能硬件'),
              '的高新技术企业，业务覆盖全国，团队规模 100-500 人，为实习生提供导师制培养与转正机会。'),
       NULL,
       CASE WHEN n <= 40 THEN 'auth'
            WHEN n <= 46 THEN 'pending'
            WHEN n <= 48 THEN 'reject'
            ELSE 'freeze' END,
       CASE WHEN n IN (47, 48) THEN '资质材料不清晰，请重新上传营业执照扫描件' ELSE NULL END,
       NOW(), NOW()
FROM seq
JOIN sys_user u ON u.account = CONCAT('company', n) AND u.user_type = 'company'
WHERE NOT EXISTS (SELECT 1 FROM company c WHERE c.user_owner = u.user_id);

-- ---------- 4. 学生档案（2022xxxx，共 49 份；2021001 已有） ----------
INSERT INTO student (user_owner, real_name, student_no, college, major, grade_year, edu_expect, create_time, update_time)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL SELECT n + 1 FROM seq WHERE n < 49
)
SELECT u.user_id,
       u.real_name,
       u.account,
       ELT(1 + (n * 3) MOD 10, '计算机学院','软件学院','电子信息学院','经济管理学院','外国语学院','机械工程学院','材料科学与工程学院','数学与统计学院','新闻传媒学院','法学院'),
       ELT(1 + (n * 7) MOD 10, '计算机科学与技术','软件工程','人工智能','电子信息工程','金融学','英语','机械设计制造及其自动化','材料成型及控制工程','数学与应用数学','网络与新媒体'),
       '2022',
       DATE_ADD('2026-06-30', INTERVAL (n MOD 2) YEAR),
       NOW(), NOW()
FROM seq
JOIN sys_user u ON u.account = CONCAT('2022', LPAD(n, 4, '0')) AND u.user_type = 'student'
WHERE NOT EXISTS (SELECT 1 FROM student s WHERE s.user_owner = u.user_id);

-- ---------- 5. 岗位（每家企业约 3 个，总量不足 150 时补齐 150 条） ----------
INSERT INTO job (company_id, job_title, job_type, headcount, work_place, category, req, duty, benefit_money, start_date, end_date, audit_state, audit_opinion, publish_time, create_time, update_time)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL SELECT n + 1 FROM seq WHERE n < 150
)
SELECT c.company_id,
       ELT(1 + MOD((1 + MOD(n - 1, 50)) * 7 + (n - 1) DIV 50, 25),
           'Java后端开发实习生','前端开发实习生','算法实习生','数据分析实习生','产品经理实习生',
           'UI设计实习生','软件测试实习生','新媒体运营实习生','市场营销实习生','人力资源实习生',
           '财务实习生','嵌入式开发实习生','机械设计实习生','电气工程实习生','法务实习生',
           '电商运营实习生','游戏开发实习生','网络安全实习生','云计算实习生','区块链开发实习生',
           '供应链管理实习生','客户成功实习生','内容运营实习生','品牌公关实习生','行政实习生'),
       ELT(1 + n MOD 3, 'teaching', 'professional', 'graduation'),
       1 + n MOD 8,
       ELT(1 + n MOD 10, '杭州','北京','上海','深圳','广州','成都','武汉','南京','西安','苏州'),
       ELT(1 + n MOD 7, '研发','产品','设计','运营','市场','数据','职能'),
       CONCAT('1. 本科及以上学历，',
              ELT(1 + n MOD 6, '计算机相关专业优先','专业不限','理工科背景优先','相关专业优先','在校成绩良好','有相关课程基础'),
              '；2. ',
              ELT(1 + n MOD 6, '熟悉至少一门编程语言','熟练使用 Office 办公软件','有项目或社团经验者优先','具备良好的沟通表达能力','学习能力强，能快速上手','有相关实习经历者优先'),
              '；3. 每周至少实习 4 天，可实习 3 个月以上。'),
       CONCAT('1. ',
              ELT(1 + n MOD 6, '参与核心业务模块的设计与开发','协助完成日常运营与数据整理','参与产品需求分析与文档撰写','配合团队完成测试与质量保障','参与市场活动策划与执行','协助部门完成日常事务性工作'),
              '；2. 完成导师交办的其他任务，定期参与团队例会与复盘。'),
       (1500 + (n MOD 40) * 100),
       DATE_ADD(CURDATE(), INTERVAL (n MOD 30) DAY),
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL (n MOD 30) DAY), INTERVAL (3 + n MOD 4) MONTH),
       CASE WHEN n MOD 20 < 13 THEN 'online'
            WHEN n MOD 20 < 16 THEN 'pending'
            WHEN n MOD 20 < 18 THEN 'draft'
            WHEN n MOD 20 = 18 THEN 'reject'
            ELSE 'ended' END,
       CASE WHEN n MOD 20 = 18 THEN '岗位描述不符合规范，请补充职责明细' ELSE NULL END,
       CASE WHEN n MOD 20 < 13 THEN DATE_SUB(NOW(), INTERVAL (n * 13) MINUTE) ELSE NULL END,
       NOW(), NOW()
FROM seq
JOIN sys_user u ON u.account = CONCAT('company', 1 + MOD(n - 1, 50)) AND u.user_type = 'company'
JOIN company c ON c.user_owner = u.user_id
WHERE (SELECT COUNT(*) FROM job) < 150;
