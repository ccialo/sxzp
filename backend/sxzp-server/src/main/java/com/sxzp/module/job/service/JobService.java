package com.sxzp.module.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.result.PageResult;
import com.sxzp.common.security.LoginUser;
import com.sxzp.common.security.LoginUserContext;
import com.sxzp.module.apply.entity.JobApply;
import com.sxzp.module.apply.mapper.JobApplyMapper;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.mapper.CompanyMapper;
import com.sxzp.module.company.service.CompanyService;
import com.sxzp.module.job.dto.JobAuditRequest;
import com.sxzp.module.job.dto.JobQueryRequest;
import com.sxzp.module.job.dto.JobSaveRequest;
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.mapper.JobMapper;
import com.sxzp.module.job.vo.JobVO;
import com.sxzp.module.student.entity.Student;
import com.sxzp.module.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 岗位服务：企业发布/维护、管理员审核、学生检索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    /** 岗位状态常量 */
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_ONLINE = "online";
    public static final String STATE_REJECT = "reject";
    public static final String STATE_ENDED = "ended";

    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final CompanyService companyService;
    private final JobApplyMapper jobApplyMapper;
    private final StudentMapper studentMapper;

    /** 企业发布/修改岗位 */
    @Transactional(rollbackFor = Exception.class)
    public Job save(JobSaveRequest request) {
        Long companyId = companyService.currentCompanyId();
        boolean submit = Boolean.TRUE.equals(request.getSubmit());
        if (submit) {
            // 只有已认证企业才能提交审核
            companyService.assertAuthenticated(companyId);
        }

        Job job;
        if (request.getJobId() == null) {
            job = new Job();
            job.setCompanyId(companyId);
        } else {
            job = jobMapper.selectById(request.getJobId());
            if (job == null) {
                throw new BizException(404, "岗位不存在");
            }
            if (!companyId.equals(job.getCompanyId())) {
                throw new BizException(403, "无权修改他人岗位");
            }
            if (STATE_ONLINE.equals(job.getAuditState())) {
                throw new BizException("在招岗位请先下线后再修改");
            }
        }

        job.setJobTitle(request.getJobTitle());
        job.setJobType(request.getJobType());
        job.setHeadcount(request.getHeadcount());
        job.setWorkPlace(request.getWorkPlace());
        job.setCategory(request.getCategory());
        job.setReq(request.getReq());
        job.setDuty(request.getDuty());
        job.setBenefitMoney(request.getBenefitMoney());
        job.setStartDate(request.getStartDate());
        job.setEndDate(request.getEndDate());
        job.setAuditState(submit ? STATE_PENDING : STATE_DRAFT);
        job.setAuditOpinion(null);

        if (job.getJobId() == null) {
            jobMapper.insert(job);
        } else {
            jobMapper.updateById(job);
        }
        log.info("岗位保存: jobId={}, state={}, companyId={}", job.getJobId(), job.getAuditState(), companyId);
        return job;
    }

    /** 管理员审核岗位：通过后上线，驳回则退回 */
    @Transactional(rollbackFor = Exception.class)
    public void audit(JobAuditRequest request) {
        Job job = jobMapper.selectById(request.getJobId());
        if (job == null) {
            throw new BizException(404, "岗位不存在");
        }
        if (!STATE_PENDING.equals(job.getAuditState())) {
            throw new BizException("该岗位当前状态不可审核");
        }
        if (Boolean.TRUE.equals(request.getPass())) {
            job.setAuditState(STATE_ONLINE);
            job.setAuditOpinion(null);
            job.setPublishTime(LocalDateTime.now());
        } else {
            job.setAuditState(STATE_REJECT);
            job.setAuditOpinion(StringUtils.defaultIfBlank(request.getOpinion(), "岗位信息不符合规范"));
        }
        jobMapper.updateById(job);
        log.info("岗位审核完成: jobId={}, state={}", job.getJobId(), job.getAuditState());
    }

    /**
     * 学生端岗位检索：仅返回已上线岗位，支持关键词/类型/地点/方向/津贴筛选与排序。
     */
    public PageResult<JobVO> search(JobQueryRequest query) {
        LambdaQueryWrapper<Job> wrapper = Wrappers.<Job>lambdaQuery()
                .eq(Job::getAuditState, STATE_ONLINE);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            // 关键词匹配岗位名称或岗位方向
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Job::getJobTitle, kw).or().like(Job::getCategory, kw));
        }
        if (StringUtils.isNotBlank(query.getJobType())) {
            wrapper.eq(Job::getJobType, query.getJobType());
        }
        if (StringUtils.isNotBlank(query.getWorkPlace())) {
            wrapper.like(Job::getWorkPlace, query.getWorkPlace().trim());
        }
        if (StringUtils.isNotBlank(query.getCategory())) {
            wrapper.like(Job::getCategory, query.getCategory().trim());
        }
        if (query.getMinBenefit() != null) {
            wrapper.ge(Job::getBenefitMoney, query.getMinBenefit());
        }
        boolean desc = !Boolean.FALSE.equals(query.getDesc());
        if ("benefit_money".equals(query.getSortBy())) {
            wrapper.orderBy(true, desc, Job::getBenefitMoney);
        } else {
            wrapper.orderBy(true, desc, Job::getPublishTime);
        }

        int current = query.getCurrent() == null || query.getCurrent() < 1 ? 1 : query.getCurrent();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);
        Page<Job> page = jobMapper.selectPage(new Page<>(current, size), wrapper);
        List<JobVO> list = toVOList(page.getRecords(), true);
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 岗位详情（学生端：附带“我是否已投递”） */
    public JobVO detail(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BizException(404, "岗位不存在");
        }
        JobVO vo = toVO(job, true);
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser != null && loginUser.isStudent()) {
            Student student = currentStudent();
            if (student != null) {
                Long count = jobApplyMapper.selectCount(Wrappers.<JobApply>lambdaQuery()
                        .eq(JobApply::getJobId, jobId)
                        .eq(JobApply::getStudentId, student.getStudentId()));
                vo.setApplied(count != null && count > 0);
            }
        }
        return vo;
    }

    /** 企业端：我的岗位列表（可按状态筛选） */
    public PageResult<JobVO> pageMyJobs(int current, int size, String auditState) {
        Long companyId = companyService.currentCompanyId();
        LambdaQueryWrapper<Job> wrapper = Wrappers.<Job>lambdaQuery()
                .eq(Job::getCompanyId, companyId)
                .orderByDesc(Job::getCreateTime);
        if (StringUtils.isNotBlank(auditState)) {
            wrapper.eq(Job::getAuditState, auditState);
        }
        Page<Job> page = jobMapper.selectPage(new Page<>(current, size), wrapper);
        List<JobVO> list = toVOList(page.getRecords(), false);
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 管理端：岗位列表（按状态/关键词） */
    public PageResult<JobVO> pageForAdmin(int current, int size, String auditState, String keyword) {
        LambdaQueryWrapper<Job> wrapper = Wrappers.<Job>lambdaQuery()
                .orderByDesc(Job::getCreateTime);
        if (StringUtils.isNotBlank(auditState)) {
            wrapper.eq(Job::getAuditState, auditState);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Job::getJobTitle, keyword.trim());
        }
        Page<Job> page = jobMapper.selectPage(new Page<>(current, size), wrapper);
        List<JobVO> list = toVOList(page.getRecords(), false);
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 校验岗位可投递（供投递模块调用） */
    public Job requireOnlineJob(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BizException(404, "岗位不存在");
        }
        if (!STATE_ONLINE.equals(job.getAuditState())) {
            throw new BizException("该岗位已下线或尚未通过审核，无法投递");
        }
        return job;
    }

    /** 当前登录学生档案 */
    private Student currentStudent() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            return null;
        }
        return studentMapper.selectOne(Wrappers.<Student>lambdaQuery()
                .eq(Student::getUserOwner, loginUser.getUserId()));
    }

    /** 单条转 VO */
    private JobVO toVO(Job job, boolean withApplyCount) {
        JobVO vo = new JobVO();
        BeanUtils.copyProperties(job, vo);
        Company company = companyMapper.selectById(job.getCompanyId());
        if (company != null) {
            vo.setCompanyName(company.getEntName());
            vo.setCompanyAuthState(company.getAuthState());
        }
        if (withApplyCount) {
            Long count = jobApplyMapper.selectCount(Wrappers.<JobApply>lambdaQuery()
                    .eq(JobApply::getJobId, job.getJobId()));
            vo.setApplyCount(count == null ? 0L : count);
        }
        return vo;
    }

    /** 批量转 VO（避免 N+1：一次性把企业与投递数查出来） */
    private List<JobVO> toVOList(List<Job> jobs, boolean withApplyCount) {
        if (jobs == null || jobs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> companyIds = jobs.stream().map(Job::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (a, b) -> a));

        // 批量统计每个岗位的投递数（一次 group by 查询，替代逐条 count）
        Map<Long, Long> applyCountMap = Collections.emptyMap();
        if (withApplyCount) {
            List<Long> jobIds = jobs.stream().map(Job::getJobId).collect(Collectors.toList());
            applyCountMap = new java.util.HashMap<>();
            List<Map<String, Object>> rows = jobApplyMapper.selectMaps(
                    new QueryWrapper<JobApply>()
                            .select("job_id", "count(*) as cnt")
                            .in("job_id", jobIds)
                            .groupBy("job_id"));
            for (Map<String, Object> row : rows) {
                Long jobId = ((Number) row.get("job_id")).longValue();
                Long cnt = ((Number) row.get("cnt")).longValue();
                applyCountMap.put(jobId, cnt);
            }
        }

        List<JobVO> result = new ArrayList<>(jobs.size());
        for (Job job : jobs) {
            JobVO vo = new JobVO();
            BeanUtils.copyProperties(job, vo);
            Company company = companyMap.get(job.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getEntName());
                vo.setCompanyAuthState(company.getAuthState());
            }
            if (withApplyCount) {
                vo.setApplyCount(applyCountMap.getOrDefault(job.getJobId(), 0L));
            }
            result.add(vo);
        }
        return result;
    }

    /** 企业下线岗位（停止接收投递） */
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BizException(404, "岗位不存在");
        }
        LoginUser loginUser = LoginUserContext.require();
        if (!loginUser.isAdmin()) {
            Long companyId = companyService.currentCompanyId();
            if (!companyId.equals(job.getCompanyId())) {
                throw new BizException(403, "无权操作他人岗位");
            }
        }
        if (!STATE_ONLINE.equals(job.getAuditState())) {
            throw new BizException("仅已上线的岗位可下线");
        }
        job.setAuditState(STATE_ENDED);
        jobMapper.updateById(job);
        log.info("岗位已下线: jobId={}", jobId);
    }

    /** 企业重新提交草稿/被驳回/已下线的岗位（发送审核） */
    @Transactional(rollbackFor = Exception.class)
    public void resubmit(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BizException(404, "岗位不存在");
        }
        Long companyId = companyService.currentCompanyId();
        if (!companyId.equals(job.getCompanyId())) {
            throw new BizException(403, "无权操作他人岗位");
        }
        if (!STATE_DRAFT.equals(job.getAuditState())
                && !STATE_REJECT.equals(job.getAuditState())
                && !STATE_ENDED.equals(job.getAuditState())) {
            throw new BizException("仅草稿、被驳回或已下线的岗位可重新提交");
        }
        companyService.assertAuthenticated(companyId);
        job.setAuditState(STATE_PENDING);
        job.setAuditOpinion(null);
        jobMapper.updateById(job);
        log.info("岗位重新提交审核: jobId={}", jobId);
    }
}
