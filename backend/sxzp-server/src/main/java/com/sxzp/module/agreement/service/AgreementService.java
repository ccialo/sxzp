package com.sxzp.module.agreement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.result.PageResult;
import com.sxzp.module.agreement.dto.AgreementSaveRequest;
import com.sxzp.module.agreement.entity.Agreement;
import com.sxzp.module.agreement.mapper.AgreementMapper;
import com.sxzp.module.agreement.vo.AgreementVO;
import com.sxzp.module.apply.entity.JobApply;
import com.sxzp.module.apply.mapper.JobApplyMapper;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.mapper.CompanyMapper;
import com.sxzp.module.company.service.CompanyService;
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.mapper.JobMapper;
import com.sxzp.module.notice.service.NoticeService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实习协议服务：企业发起、学生签署/拒绝。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgreementService {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_SIGNED = "signed";
    public static final String STATE_REJECTED = "rejected";

    private final AgreementMapper agreementMapper;
    private final JobApplyMapper applyMapper;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final StudentMapper studentMapper;
    private final CompanyService companyService;
    private final ResumeService resumeService;
    private final NoticeService noticeService;

    /**
     * 企业发起实习协议：基于投递记录生成协议草稿。
     */
    @Transactional(rollbackFor = Exception.class)
    public Agreement save(AgreementSaveRequest request) {
        JobApply apply = applyMapper.selectById(request.getApplyId());
        if (apply == null) {
            throw new BizException(404, "投递记录不存在");
        }
        Long companyId = companyService.currentCompanyId();
        Job job = jobMapper.selectById(apply.getJobId());
        if (job == null || !companyId.equals(job.getCompanyId())) {
            throw new BizException(403, "无权为该投递发起协议");
        }
        // 同一投递只能有一份有效协议
        Long exist = agreementMapper.selectCount(Wrappers.<Agreement>lambdaQuery()
                .eq(Agreement::getApplyId, apply.getApplyId())
                .in(Agreement::getState, STATE_PENDING, STATE_SIGNED));
        if (exist != null && exist > 0) {
            throw new BizException("该投递已存在有效协议，请勿重复发起");
        }

        Agreement agreement = new Agreement();
        agreement.setApplyId(apply.getApplyId());
        agreement.setJobId(apply.getJobId());
        agreement.setStudentId(apply.getStudentId());
        agreement.setCompanyId(companyId);
        agreement.setStartDate(request.getStartDate());
        agreement.setEndDate(request.getEndDate());
        agreement.setSalary(request.getSalary());
        agreement.setContent(request.getContent());
        agreement.setState(STATE_PENDING);
        agreementMapper.insert(agreement);

        // 通知学生
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student != null) {
            noticeService.send(student.getUserOwner(), "实习协议待签署",
                    "企业「" + job.getJobTitle() + "」已向您发起实习协议，请及时处理。",
                    NoticeService.TYPE_AGREEMENT);
        }
        log.info("实习协议已发起: agreementId={}, applyId={}", agreement.getAgreementId(), apply.getApplyId());
        return agreement;
    }

    /** 学生：我的协议列表 */
    public PageResult<AgreementVO> pageMine(int current, int size, String state) {
        Student student = resumeService.currentStudent();
        LambdaQueryWrapper<Agreement> wrapper = Wrappers.<Agreement>lambdaQuery()
                .eq(Agreement::getStudentId, student.getStudentId())
                .orderByDesc(Agreement::getCreateTime);
        if (StringUtils.isNotBlank(state)) {
            wrapper.eq(Agreement::getState, state);
        }
        Page<Agreement> page = agreementMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toVOList(page.getRecords(), true), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业：我发出的协议列表 */
    public PageResult<AgreementVO> pageSent(int current, int size, String state) {
        Long companyId = companyService.currentCompanyId();
        LambdaQueryWrapper<Agreement> wrapper = Wrappers.<Agreement>lambdaQuery()
                .eq(Agreement::getCompanyId, companyId)
                .orderByDesc(Agreement::getCreateTime);
        if (StringUtils.isNotBlank(state)) {
            wrapper.eq(Agreement::getState, state);
        }
        Page<Agreement> page = agreementMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toVOList(page.getRecords(), false), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 学生签署协议 */
    @Transactional(rollbackFor = Exception.class)
    public void sign(Long agreementId) {
        Agreement agreement = requireMine(agreementId, true);
        if (!STATE_PENDING.equals(agreement.getState())) {
            throw new BizException("当前状态不可签署");
        }
        agreement.setState(STATE_SIGNED);
        agreement.setSignTime(LocalDateTime.now());
        agreementMapper.updateById(agreement);

        // 投递状态推进为已签署协议
        JobApply apply = applyMapper.selectById(agreement.getApplyId());
        if (apply != null) {
            apply.setApplyState("agreement");
            applyMapper.updateById(apply);
        }
        // 通知企业
        Company company = companyMapper.selectById(agreement.getCompanyId());
        if (company != null) {
            noticeService.send(company.getUserOwner(), "实习协议已签署",
                    "学生已签署您发起的实习协议。",
                    NoticeService.TYPE_AGREEMENT);
        }
        log.info("实习协议已签署: agreementId={}", agreementId);
    }

    /** 学生拒绝协议 */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long agreementId) {
        Agreement agreement = requireMine(agreementId, true);
        if (!STATE_PENDING.equals(agreement.getState())) {
            throw new BizException("当前状态不可拒绝");
        }
        agreement.setState(STATE_REJECTED);
        agreementMapper.updateById(agreement);
        // 通知企业
        Company company = companyMapper.selectById(agreement.getCompanyId());
        if (company != null) {
            noticeService.send(company.getUserOwner(), "实习协议被拒绝",
                    "学生拒绝了您发起的实习协议。",
                    NoticeService.TYPE_AGREEMENT);
        }
        log.info("实习协议被拒绝: agreementId={}", agreementId);
    }

    /** 校验协议归属：forStudent=true 校验学生，否则校验企业 */
    private Agreement requireMine(Long agreementId, boolean forStudent) {
        Agreement agreement = agreementMapper.selectById(agreementId);
        if (agreement == null) {
            throw new BizException(404, "协议不存在");
        }
        if (forStudent) {
            Student student = resumeService.currentStudent();
            if (!student.getStudentId().equals(agreement.getStudentId())) {
                throw new BizException(403, "无权操作他人协议");
            }
        } else {
            Long companyId = companyService.currentCompanyId();
            if (!companyId.equals(agreement.getCompanyId())) {
                throw new BizException(403, "无权操作他人协议");
            }
        }
        return agreement;
    }

    private List<AgreementVO> toVOList(List<Agreement> agreements, boolean forStudent) {
        if (agreements == null || agreements.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> jobIds = agreements.stream().map(Agreement::getJobId).distinct().collect(Collectors.toList());
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, Function.identity(), (a, b) -> a));

        List<Long> companyIds = agreements.stream().map(Agreement::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (a, b) -> a));

        List<Long> studentIds = agreements.stream().map(Agreement::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, Student> studentMap = studentIds.isEmpty() ? Collections.emptyMap()
                : studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity(), (a, b) -> a));

        List<AgreementVO> list = new ArrayList<>(agreements.size());
        for (Agreement a : agreements) {
            AgreementVO vo = new AgreementVO();
            vo.setAgreementId(a.getAgreementId());
            vo.setApplyId(a.getApplyId());
            vo.setJobId(a.getJobId());
            vo.setStudentId(a.getStudentId());
            vo.setCompanyId(a.getCompanyId());
            vo.setStartDate(a.getStartDate());
            vo.setEndDate(a.getEndDate());
            vo.setSalary(a.getSalary());
            vo.setContent(a.getContent());
            vo.setState(a.getState());
            vo.setSignTime(a.getSignTime());
            vo.setCreateTime(a.getCreateTime());
            Job job = jobMap.get(a.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobTitle());
            }
            Company company = companyMap.get(a.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getEntName());
            }
            if (!forStudent) {
                Student student = studentMap.get(a.getStudentId());
                if (student != null) {
                    vo.setStudentName(student.getRealName());
                    vo.setStudentNo(student.getStudentNo());
                    vo.setCollege(student.getCollege());
                }
            }
            list.add(vo);
        }
        return list;
    }
}
