package com.sxzp.module.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxzp.module.agreement.entity.Agreement;
import com.sxzp.module.agreement.mapper.AgreementMapper;
import com.sxzp.module.apply.entity.JobApply;
import com.sxzp.module.apply.mapper.JobApplyMapper;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.mapper.CompanyMapper;
import com.sxzp.module.interview.entity.Interview;
import com.sxzp.module.interview.mapper.InterviewMapper;
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.mapper.JobMapper;
import com.sxzp.module.student.entity.Student;
import com.sxzp.module.student.mapper.StudentMapper;
import com.sxzp.module.stats.vo.ApplySuccessVO;
import com.sxzp.module.stats.vo.ApplyTrendVO;
import com.sxzp.module.stats.vo.DashboardVO;
import com.sxzp.module.stats.vo.JobRankingVO;
import com.sxzp.module.stats.vo.JobTypeSuccessVO;
import com.sxzp.module.stats.vo.NameCountVO;
import com.sxzp.module.stats.vo.RecentApplyVO;
import com.sxzp.module.weekly.entity.WeeklyReport;
import com.sxzp.module.weekly.mapper.WeeklyReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计报表服务：管理端数据看板、热门岗位排行、投递趋势。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudentMapper studentMapper;
    private final CompanyMapper companyMapper;
    private final JobMapper jobMapper;
    private final JobApplyMapper applyMapper;
    private final InterviewMapper interviewMapper;
    private final AgreementMapper agreementMapper;
    private final WeeklyReportMapper weeklyReportMapper;

    /** 仪表盘总览 */
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();

        // 基础数据
        vo.setStudentCount(studentMapper.selectCount(null));
        vo.setCompanyCount(companyMapper.selectCount(null));
        vo.setCompanyAuthedCount(companyMapper.selectCount(
                Wrappers.<Company>lambdaQuery().eq(Company::getAuthState, "auth")));
        vo.setJobCount(jobMapper.selectCount(null));
        vo.setJobOnlineCount(jobMapper.selectCount(
                Wrappers.<Job>lambdaQuery().eq(Job::getAuditState, "online")));

        // 投递统计
        vo.setApplyCount(applyMapper.selectCount(null));
        vo.setApplyStateCount(countGroupBy(applyMapper, "apply_state"));
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayApplyCount(applyMapper.selectCount(
                Wrappers.<JobApply>lambdaQuery().ge(JobApply::getApplyTime, todayStart)));

        // 面试统计
        vo.setInterviewCount(interviewMapper.selectCount(null));
        vo.setInterviewStateCount(countGroupBy(interviewMapper, "state"));

        // 协议统计
        vo.setAgreementCount(agreementMapper.selectCount(null));
        vo.setAgreementSignedCount(agreementMapper.selectCount(
                Wrappers.<Agreement>lambdaQuery().eq(Agreement::getState, "signed")));

        // 周报统计
        vo.setWeeklyReportCount(weeklyReportMapper.selectCount(null));
        vo.setWeeklyReviewedCount(weeklyReportMapper.selectCount(
                Wrappers.<WeeklyReport>lambdaQuery().eq(WeeklyReport::getState, "reviewed")));

        return vo;
    }

    /**
     * 热门岗位排行（按投递数降序，取前 N）。
     */
    public List<JobRankingVO> jobRanking(int limit) {
        // 批量统计每个岗位的投递数
        List<Map<String, Object>> rows = applyMapper.selectMaps(
                new QueryWrapper<JobApply>()
                        .select("job_id", "count(*) as cnt")
                        .groupBy("job_id")
                        .orderByDesc("cnt")
                        .last("limit " + limit));

        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> jobIds = rows.stream()
                .map(r -> ((Number) r.get("job_id")).longValue())
                .collect(Collectors.toList());
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, j -> j, (a, b) -> a));

        // 批量查企业名
        List<Long> companyIds = jobMap.values().stream()
                .map(Job::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, c -> c, (a, b) -> a));

        List<JobRankingVO> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Long jobId = ((Number) row.get("job_id")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            Job job = jobMap.get(jobId);
            if (job == null) {
                continue;
            }
            JobRankingVO vo = new JobRankingVO();
            vo.setJobId(jobId);
            vo.setJobTitle(job.getJobTitle());
            vo.setApplyCount(cnt);
            vo.setHeadcount(job.getHeadcount());
            vo.setAuditState(job.getAuditState());
            Company company = companyMap.get(job.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getEntName());
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 投递趋势（近 N 天每日投递数）。
     */
    public List<ApplyTrendVO> applyTrend(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1L);
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);

        List<Map<String, Object>> rows = applyMapper.selectMaps(
                new QueryWrapper<JobApply>()
                        .select("DATE(apply_time) as d", "count(*) as cnt")
                        .ge("apply_time", start)
                        .groupBy("DATE(apply_time)")
                        .orderByAsc("d"));

        Map<LocalDate, Long> dataMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate date;
            Object d = row.get("d");
            if (d instanceof java.sql.Date) {
                date = ((java.sql.Date) d).toLocalDate();
            } else if (d instanceof LocalDate) {
                date = (LocalDate) d;
            } else {
                date = LocalDate.parse(d.toString());
            }
            Long cnt = ((Number) row.get("cnt")).longValue();
            dataMap.put(date, cnt);
        }

        // 填充没有投递的日期为 0
        List<ApplyTrendVO> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            ApplyTrendVO vo = new ApplyTrendVO();
            vo.setDate(date);
            vo.setCount(dataMap.getOrDefault(date, 0L));
            result.add(vo);
        }
        return result;
    }

    /**
     * 岗位类型分布（各类型岗位数）。
     */
    public List<NameCountVO> jobTypeDistribution() {
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.select("job_type AS name", "COUNT(*) AS value")
                .isNotNull("job_type")
                .ne("job_type", "")
                .groupBy("job_type")
                .orderByDesc("value");
        return toNameCountList(jobMapper.selectMaps(wrapper));
    }

    /**
     * 学生学院分布（取前 N 个学院）。
     */
    public List<NameCountVO> collegeDistribution(int limit) {
        int n = Math.max(1, Math.min(limit, 20));
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.select("college AS name", "COUNT(*) AS value")
                .isNotNull("college")
                .ne("college", "")
                .groupBy("college")
                .orderByDesc("value")
                .last("LIMIT " + n);
        return toNameCountList(studentMapper.selectMaps(wrapper));
    }

    /**
     * 企业认证状态分布。
     */
    public List<NameCountVO> companyAuthDistribution() {
        QueryWrapper<Company> wrapper = new QueryWrapper<>();
        wrapper.select("auth_state AS name", "COUNT(*) AS value")
                .groupBy("auth_state")
                .orderByDesc("value");
        return toNameCountList(companyMapper.selectMaps(wrapper));
    }

    /**
     * 学生投递成功率（整体 + 按岗位类型）。
     * 成功口径：投递进入面试及之后环节（interview / agreement / hired）。
     */
    public ApplySuccessVO applySuccess() {
        List<JobApply> applies = applyMapper.selectList(
                new QueryWrapper<JobApply>().select("apply_id", "job_id", "apply_state"));
        Map<Long, String> jobTypeMap = new HashMap<>();
        for (Job job : jobMapper.selectList(new QueryWrapper<Job>().select("job_id", "job_type"))) {
            jobTypeMap.put(job.getJobId(), job.getJobType());
        }

        long total = applies.size();
        long success = 0;
        Map<String, long[]> byType = new LinkedHashMap<>();
        for (JobApply apply : applies) {
            boolean ok = isSuccessState(apply.getApplyState());
            if (ok) {
                success++;
            }
            String jobType = jobTypeMap.get(apply.getJobId());
            if (jobType == null || jobType.isEmpty()) {
                continue;
            }
            long[] pair = byType.computeIfAbsent(jobType, k -> new long[2]);
            pair[0]++;
            if (ok) {
                pair[1]++;
            }
        }

        ApplySuccessVO vo = new ApplySuccessVO();
        vo.setTotal(total);
        vo.setSuccess(success);
        vo.setRate(percent(success, total));
        List<JobTypeSuccessVO> rates = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : byType.entrySet()) {
            long[] pair = entry.getValue();
            JobTypeSuccessVO item = new JobTypeSuccessVO();
            item.setJobType(entry.getKey());
            item.setTotal(pair[0]);
            item.setSuccess(pair[1]);
            item.setRate(percent(pair[1], pair[0]));
            rates.add(item);
        }
        rates.sort((a, b) -> Long.compare(b.getTotal(), a.getTotal()));
        vo.setJobTypeRates(rates);
        return vo;
    }

    /**
     * 最近投递动态。
     */
    public List<RecentApplyVO> recentApplies(int limit) {
        int n = Math.max(1, Math.min(limit, 50));
        Page<JobApply> page = applyMapper.selectPage(new Page<>(1, n),
                Wrappers.<JobApply>lambdaQuery().orderByDesc(JobApply::getApplyTime));
        List<JobApply> applies = page.getRecords();
        if (applies == null || applies.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> studentIds = applies.stream().map(JobApply::getStudentId).distinct().collect(Collectors.toList());
        List<Long> jobIds = applies.stream().map(JobApply::getJobId).distinct().collect(Collectors.toList());
        Map<Long, Student> studentMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, s -> s, (a, b) -> a));
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, j -> j, (a, b) -> a));
        List<Long> companyIds = jobMap.values().stream().map(Job::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, c -> c, (a, b) -> a));

        List<RecentApplyVO> list = new ArrayList<>(applies.size());
        for (JobApply apply : applies) {
            RecentApplyVO vo = new RecentApplyVO();
            vo.setApplyId(apply.getApplyId());
            vo.setApplyState(apply.getApplyState());
            vo.setApplyTime(apply.getApplyTime());
            Student student = studentMap.get(apply.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
                vo.setStudentNo(student.getStudentNo());
                vo.setCollege(student.getCollege());
            }
            Job job = jobMap.get(apply.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobTitle());
                Company company = companyMap.get(job.getCompanyId());
                if (company != null) {
                    vo.setCompanyName(company.getEntName());
                }
            }
            list.add(vo);
        }
        return list;
    }

    /** 投递成功状态：进入面试及之后环节 */
    private static boolean isSuccessState(String state) {
        return "interview".equals(state) || "agreement".equals(state) || "hired".equals(state);
    }

    /** 百分比（保留 1 位小数），返回 0-100 的数值 */
    private static double percent(long part, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round(part * 1000.0 / total) / 10.0;
    }

    /** selectMaps 结果转 NameCountVO 列表 */
    private List<NameCountVO> toNameCountList(List<Map<String, Object>> rows) {
        List<NameCountVO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> row : rows) {
            NameCountVO vo = new NameCountVO();
            Object name = row.get("name");
            Object value = row.get("value");
            vo.setName(name == null ? null : name.toString());
            vo.setValue(value == null ? 0L : ((Number) value).longValue());
            list.add(vo);
        }
        return list;
    }

    /**
     * 通用：对某表的指定列做 group by count，返回 Map<值, 数量>。
     */
    private <T> Map<String, Long> countGroupBy(
            com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, String column) {
        // 使用 selectMaps + QueryWrapper 实现 group by
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.select(column, "count(*) as cnt").groupBy(column);
        List<Map<String, Object>> rows = mapper.selectMaps(wrapper);
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String key = row.get(column) == null ? "null" : row.get(column).toString();
            Long cnt = ((Number) row.get("cnt")).longValue();
            result.put(key, cnt);
        }
        return result;
    }
}
