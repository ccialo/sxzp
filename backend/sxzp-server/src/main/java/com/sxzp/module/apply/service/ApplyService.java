package com.sxzp.module.apply.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.result.PageResult;
import com.sxzp.module.apply.dto.ApplyAuditRequest;
import com.sxzp.module.apply.dto.ApplySubmitRequest;
import com.sxzp.module.apply.entity.JobApply;
import com.sxzp.module.apply.mapper.JobApplyMapper;
import com.sxzp.module.apply.vo.ApplyVO;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.mapper.CompanyMapper;
import com.sxzp.module.company.service.CompanyService;
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.mapper.JobMapper;
import com.sxzp.module.job.service.JobService;
import com.sxzp.module.resume.entity.StudentResume;
import com.sxzp.module.resume.mapper.StudentResumeMapper;
import com.sxzp.module.resume.service.ResumeService;
import com.sxzp.module.student.entity.Student;
import com.sxzp.module.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 投递服务：学生投递/查询，企业筛选。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_INTERVIEW = "interview";
    public static final String STATE_REJECTED = "rejected";
    public static final String STATE_WITHDRAWN = "withdrawn";

    /** 企业可操作的目标状态 */
    private static final List<String> COMPANY_EDITABLE_STATES = Arrays.asList(STATE_INTERVIEW, STATE_REJECTED);

    private final JobApplyMapper applyMapper;
    private final JobMapper jobMapper;
    private final JobService jobService;
    private final CompanyMapper companyMapper;
    private final CompanyService companyService;
    private final StudentMapper studentMapper;
    private final StudentResumeMapper resumeMapper;
    private final ResumeService resumeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 学生投递简历：岗位须在招，简历须属于本人，同一岗位同一简历不可重复投递。
     */
    @Transactional(rollbackFor = Exception.class)
    public JobApply submit(ApplySubmitRequest request) {
        Job job = jobService.requireOnlineJob(request.getJobId());
        Student student = resumeService.currentStudent();

        StudentResume resume;
        if (request.getResumeId() != null) {
            resume = resumeMapper.selectById(request.getResumeId());
            if (resume == null) {
                throw new BizException(404, "简历不存在");
            }
            if (!student.getStudentId().equals(resume.getStudentId())) {
                throw new BizException(403, "只能投递本人的简历");
            }
        } else {
            resume = resumeService.defaultResume(student.getStudentId());
            if (resume == null) {
                throw new BizException("请先创建一份简历再投递");
            }
        }

        Long dup = applyMapper.selectCount(Wrappers.<JobApply>lambdaQuery()
                .eq(JobApply::getJobId, job.getJobId())
                .eq(JobApply::getResumeId, resume.getResumeId()));
        if (dup != null && dup > 0) {
            throw new BizException("您已使用该简历投递过此岗位，请勿重复投递");
        }
        // 招满校验
        if (job.getHeadcount() != null) {
            Long hired = applyMapper.selectCount(Wrappers.<JobApply>lambdaQuery()
                    .eq(JobApply::getJobId, job.getJobId())
                    .eq(JobApply::getApplyState, "hired"));
            if (hired != null && hired >= job.getHeadcount()) {
                throw new BizException("该岗位已招满，无法继续投递");
            }
        }

        JobApply apply = new JobApply();
        apply.setJobId(job.getJobId());
        apply.setStudentId(student.getStudentId());
        apply.setResumeId(resume.getResumeId());
        apply.setApplyState(STATE_PENDING);
        apply.setApplyTime(LocalDateTime.now());
        apply.setResumeSnapshot(toSnapshot(resume, student));
        applyMapper.insert(apply);
        log.info("学生投递成功: applyId={}, jobId={}, studentId={}",
                apply.getApplyId(), job.getJobId(), student.getStudentId());
        return apply;
    }

    /** 学生撤回投递（仅待处理状态可撤回） */
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long applyId) {
        JobApply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw new BizException(404, "投递记录不存在");
        }
        Student student = resumeService.currentStudent();
        if (!student.getStudentId().equals(apply.getStudentId())) {
            throw new BizException(403, "无权操作他人投递记录");
        }
        if (!STATE_PENDING.equals(apply.getApplyState())) {
            throw new BizException("该投递已被处理，无法撤回");
        }
        apply.setApplyState(STATE_WITHDRAWN);
        applyMapper.updateById(apply);
    }

    /** 学生：我的投递列表 */
    public PageResult<ApplyVO> pageMyApplies(int current, int size, String applyState) {
        Student student = resumeService.currentStudent();
        LambdaQueryWrapper<JobApply> wrapper = Wrappers.<JobApply>lambdaQuery()
                .eq(JobApply::getStudentId, student.getStudentId())
                .orderByDesc(JobApply::getApplyTime);
        if (StringUtils.isNotBlank(applyState)) {
            wrapper.eq(JobApply::getApplyState, applyState);
        }
        Page<JobApply> page = applyMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toStudentVOList(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业：收到的投递列表（可按岗位/状态筛选） */
    public PageResult<ApplyVO> pageReceived(int current, int size, Long jobId, String applyState) {
        Long companyId = companyService.currentCompanyId();
        // 先取出本企业岗位，避免越权查看他人投递
        List<Job> jobs = jobMapper.selectList(Wrappers.<Job>lambdaQuery().eq(Job::getCompanyId, companyId));
        if (jobs.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0, current, size);
        }
        List<Long> jobIds = jobs.stream().map(Job::getJobId).collect(Collectors.toList());
        LambdaQueryWrapper<JobApply> wrapper = Wrappers.<JobApply>lambdaQuery()
                .in(JobApply::getJobId, jobId != null ? Collections.singletonList(jobId) : jobIds)
                .orderByDesc(JobApply::getApplyTime);
        if (jobId != null && !jobIds.contains(jobId)) {
            throw new BizException(403, "无权查看该岗位的投递");
        }
        if (StringUtils.isNotBlank(applyState)) {
            wrapper.eq(JobApply::getApplyState, applyState);
        }
        Page<JobApply> page = applyMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toCompanyVOList(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业处理投递：进入面试 / 淘汰 */
    @Transactional(rollbackFor = Exception.class)
    public void audit(ApplyAuditRequest request) {
        JobApply apply = applyMapper.selectById(request.getApplyId());
        if (apply == null) {
            throw new BizException(404, "投递记录不存在");
        }
        if (!COMPANY_EDITABLE_STATES.contains(request.getApplyState())) {
            throw new BizException("不支持的目标状态：" + request.getApplyState());
        }
        // 校验该投递属于本企业岗位
        Long companyId = companyService.currentCompanyId();
        Job job = jobMapper.selectById(apply.getJobId());
        if (job == null || !companyId.equals(job.getCompanyId())) {
            throw new BizException(403, "无权处理该投递");
        }
        apply.setApplyState(request.getApplyState());
        applyMapper.updateById(apply);
        log.info("投递处理: applyId={}, state={}", apply.getApplyId(), request.getApplyState());
    }

    /** 生成简历快照 */
    private String toSnapshot(StudentResume resume, Student student) {
        try {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("resumeId", resume.getResumeId());
            map.put("title", resume.getTitle());
            map.put("studentName", student.getRealName());
            map.put("studentNo", student.getStudentNo());
            map.put("college", student.getCollege());
            map.put("major", student.getMajor());
            map.put("skill", resume.getSkill());
            map.put("projectExp", resume.getProjectExp());
            map.put("selfEval", resume.getSelfEval());
            map.put("eduJson", resume.getEduJson());
            map.put("cont", resume.getCont());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("生成简历快照失败: {}", e.getMessage());
            return null;
        }
    }

    private List<ApplyVO> toStudentVOList(List<JobApply> applies) {
        if (applies == null || applies.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> jobIds = applies.stream().map(JobApply::getJobId).distinct().collect(Collectors.toList());
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, Function.identity(), (a, b) -> a));
        List<Long> companyIds = jobMap.values().stream().map(Job::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (a, b) -> a));
        List<ApplyVO> list = new ArrayList<>(applies.size());
        for (JobApply apply : applies) {
            ApplyVO vo = new ApplyVO();
            vo.setApplyId(apply.getApplyId());
            vo.setJobId(apply.getJobId());
            vo.setResumeId(apply.getResumeId());
            vo.setApplyState(apply.getApplyState());
            vo.setApplyTime(apply.getApplyTime());
            Job job = jobMap.get(apply.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobTitle());
                vo.setWorkPlace(job.getWorkPlace());
                vo.setJobType(job.getJobType());
                vo.setCompanyId(job.getCompanyId());
                Company company = companyMap.get(job.getCompanyId());
                if (company != null) {
                    vo.setCompanyName(company.getEntName());
                }
            }
            list.add(vo);
        }
        return list;
    }

    private List<ApplyVO> toCompanyVOList(List<JobApply> applies) {
        if (applies == null || applies.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> jobIds = applies.stream().map(JobApply::getJobId).distinct().collect(Collectors.toList());
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, Function.identity(), (a, b) -> a));
        List<Long> studentIds = applies.stream().map(JobApply::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, Student> studentMap = studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity(), (a, b) -> a));
        List<Long> resumeIds = applies.stream().map(JobApply::getResumeId).distinct().collect(Collectors.toList());
        Map<Long, StudentResume> resumeMap = resumeMapper.selectBatchIds(resumeIds).stream()
                .collect(Collectors.toMap(StudentResume::getResumeId, Function.identity(), (a, b) -> a));
        List<ApplyVO> list = new ArrayList<>(applies.size());
        for (JobApply apply : applies) {
            ApplyVO vo = new ApplyVO();
            vo.setApplyId(apply.getApplyId());
            vo.setJobId(apply.getJobId());
            vo.setStudentId(apply.getStudentId());
            vo.setResumeId(apply.getResumeId());
            vo.setApplyState(apply.getApplyState());
            vo.setApplyTime(apply.getApplyTime());
            vo.setResumeSnapshot(apply.getResumeSnapshot());
            Job job = jobMap.get(apply.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobTitle());
                vo.setWorkPlace(job.getWorkPlace());
                vo.setJobType(job.getJobType());
                vo.setCompanyId(job.getCompanyId());
            }
            Student student = studentMap.get(apply.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
                vo.setStudentNo(student.getStudentNo());
                vo.setCollege(student.getCollege());
                vo.setMajor(student.getMajor());
            }
            StudentResume resume = resumeMap.get(apply.getResumeId());
            if (resume != null) {
                vo.setResumeTitle(resume.getTitle());
            }
            list.add(vo);
        }
        return list;
    }
}
