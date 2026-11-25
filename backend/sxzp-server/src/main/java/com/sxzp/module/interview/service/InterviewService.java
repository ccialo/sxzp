package com.sxzp.module.interview.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.sxzp.module.interview.dto.InterviewSaveRequest;
import com.sxzp.module.interview.entity.Interview;
import com.sxzp.module.interview.mapper.InterviewMapper;
import com.sxzp.module.interview.vo.InterviewVO;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 面试安排服务：企业发起、学生确认、双方取消/完成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_CONFIRMED = "confirmed";
    public static final String STATE_CANCELLED = "cancelled";
    public static final String STATE_DONE = "done";

    private final InterviewMapper interviewMapper;
    private final JobApplyMapper applyMapper;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final StudentMapper studentMapper;
    private final CompanyService companyService;
    private final ResumeService resumeService;
    private final NoticeService noticeService;

    /**
     * 企业安排面试：基于投递记录发起，同时把投递状态推进为 interview。
     */
    @Transactional(rollbackFor = Exception.class)
    public Interview schedule(InterviewSaveRequest request) {
        JobApply apply = applyMapper.selectById(request.getApplyId());
        if (apply == null) {
            throw new BizException(404, "投递记录不存在");
        }
        Long companyId = companyService.currentCompanyId();
        Job job = jobMapper.selectById(apply.getJobId());
        if (job == null || !companyId.equals(job.getCompanyId())) {
            throw new BizException(403, "无权为该投递安排面试");
        }

        Interview interview = new Interview();
        interview.setApplyId(apply.getApplyId());
        interview.setJobId(apply.getJobId());
        interview.setStudentId(apply.getStudentId());
        interview.setCompanyId(companyId);
        interview.setInterviewTime(request.getInterviewTime());
        interview.setPlace(request.getPlace());
        interview.setContact(request.getContact());
        interview.setContactPhone(request.getContactPhone());
        interview.setRemark(request.getRemark());
        interview.setState(STATE_PENDING);
        interviewMapper.insert(interview);

        // 投递状态推进为面试中（仅当仍为待处理时）
        if ("pending".equals(apply.getApplyState())) {
            apply.setApplyState("interview");
            applyMapper.updateById(apply);
        }
        // 通知学生
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student != null) {
            noticeService.send(student.getUserOwner(), "面试邀请",
                    "企业「" + job.getJobTitle() + "」已向您发出面试邀请，请及时确认。",
                    NoticeService.TYPE_INTERVIEW);
        }
        log.info("面试安排成功: interviewId={}, applyId={}, studentId={}",
                interview.getInterviewId(), apply.getApplyId(), apply.getStudentId());
        return interview;
    }

    /** 学生：我的面试列表 */
    public PageResult<InterviewVO> pageMyInterviews(int current, int size, String state) {
        Student student = resumeService.currentStudent();
        LambdaQueryWrapper<Interview> wrapper = Wrappers.<Interview>lambdaQuery()
                .eq(Interview::getStudentId, student.getStudentId())
                .orderByDesc(Interview::getCreateTime);
        if (StringUtils.isNotBlank(state)) {
            wrapper.eq(Interview::getState, state);
        }
        Page<Interview> page = interviewMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toVOList(page.getRecords(), true), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业：我发出的面试列表 */
    public PageResult<InterviewVO> pageSentInterviews(int current, int size, String state) {
        Long companyId = companyService.currentCompanyId();
        LambdaQueryWrapper<Interview> wrapper = Wrappers.<Interview>lambdaQuery()
                .eq(Interview::getCompanyId, companyId)
                .orderByDesc(Interview::getCreateTime);
        if (StringUtils.isNotBlank(state)) {
            wrapper.eq(Interview::getState, state);
        }
        Page<Interview> page = interviewMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toVOList(page.getRecords(), false), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 学生确认面试 */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long interviewId) {
        Interview interview = requireMine(interviewId, true);
        if (!STATE_PENDING.equals(interview.getState())) {
            throw new BizException("当前状态不可确认");
        }
        interview.setState(STATE_CONFIRMED);
        interviewMapper.updateById(interview);
        // 通知企业
        Company company = companyMapper.selectById(interview.getCompanyId());
        if (company != null) {
            noticeService.send(company.getUserOwner(), "面试已确认",
                    "学生已确认您安排的面试。",
                    NoticeService.TYPE_INTERVIEW);
        }
        log.info("面试已确认: interviewId={}", interviewId);
    }

    /** 取消面试（学生可取消自己的待确认/已确认面试；企业可取消自己发起的待确认/已确认面试） */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long interviewId) {
        LoginUser loginUser = LoginUserContext.require();
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BizException(404, "面试记录不存在");
        }
        if (loginUser.isStudent()) {
            Student student = resumeService.currentStudent();
            if (!student.getStudentId().equals(interview.getStudentId())) {
                throw new BizException(403, "无权操作他人面试");
            }
        } else if (loginUser.isCompany()) {
            Long companyId = companyService.currentCompanyId();
            if (!companyId.equals(interview.getCompanyId())) {
                throw new BizException(403, "无权操作他人面试");
            }
        } else {
            throw new BizException(403, "无权操作");
        }
        if (STATE_DONE.equals(interview.getState()) || STATE_CANCELLED.equals(interview.getState())) {
            throw new BizException("当前状态不可取消");
        }
        interview.setState(STATE_CANCELLED);
        interviewMapper.updateById(interview);
        // 通知对方
        if (loginUser.isStudent()) {
            Company company = companyMapper.selectById(interview.getCompanyId());
            if (company != null) {
                noticeService.send(company.getUserOwner(), "面试已取消",
                        "学生取消了您安排的面试。",
                        NoticeService.TYPE_INTERVIEW);
            }
        } else {
            Student student = studentMapper.selectById(interview.getStudentId());
            if (student != null) {
                noticeService.send(student.getUserOwner(), "面试已取消",
                        "企业取消了安排的面试。",
                        NoticeService.TYPE_INTERVIEW);
            }
        }
        log.info("面试已取消: interviewId={}", interviewId);
    }

    /** 企业标记面试完成 */
    @Transactional(rollbackFor = Exception.class)
    public void done(Long interviewId) {
        Interview interview = requireMine(interviewId, false);
        if (!STATE_CONFIRMED.equals(interview.getState())) {
            throw new BizException("仅已确认的面试可标记完成");
        }
        interview.setState(STATE_DONE);
        interviewMapper.updateById(interview);
        // 通知学生
        Student student = studentMapper.selectById(interview.getStudentId());
        if (student != null) {
            noticeService.send(student.getUserOwner(), "面试已完成",
                    "企业标记您的面试已完成。",
                    NoticeService.TYPE_INTERVIEW);
        }
        log.info("面试已完成: interviewId={}", interviewId);
    }

    /** 校验面试归属：forStudent=true 校验学生，否则校验企业 */
    private Interview requireMine(Long interviewId, boolean forStudent) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BizException(404, "面试记录不存在");
        }
        if (forStudent) {
            Student student = resumeService.currentStudent();
            if (!student.getStudentId().equals(interview.getStudentId())) {
                throw new BizException(403, "无权操作他人面试");
            }
        } else {
            Long companyId = companyService.currentCompanyId();
            if (!companyId.equals(interview.getCompanyId())) {
                throw new BizException(403, "无权操作他人面试");
            }
        }
        return interview;
    }

    private List<InterviewVO> toVOList(List<Interview> interviews, boolean forStudent) {
        if (interviews == null || interviews.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> jobIds = interviews.stream().map(Interview::getJobId).distinct().collect(Collectors.toList());
        Map<Long, Job> jobMap = jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, Function.identity(), (a, b) -> a));

        List<Long> companyIds = interviews.stream().map(Interview::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (a, b) -> a));

        List<Long> studentIds = interviews.stream().map(Interview::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, Student> studentMap = studentIds.isEmpty() ? Collections.emptyMap()
                : studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity(), (a, b) -> a));

        List<InterviewVO> list = new ArrayList<>(interviews.size());
        for (Interview it : interviews) {
            InterviewVO vo = new InterviewVO();
            vo.setInterviewId(it.getInterviewId());
            vo.setApplyId(it.getApplyId());
            vo.setJobId(it.getJobId());
            vo.setStudentId(it.getStudentId());
            vo.setCompanyId(it.getCompanyId());
            vo.setInterviewTime(it.getInterviewTime());
            vo.setPlace(it.getPlace());
            vo.setContact(it.getContact());
            vo.setContactPhone(it.getContactPhone());
            vo.setRemark(it.getRemark());
            vo.setState(it.getState());
            vo.setCreateTime(it.getCreateTime());
            Job job = jobMap.get(it.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobTitle());
            }
            Company company = companyMap.get(it.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getEntName());
            }
            // 企业视角需要学生信息
            if (!forStudent) {
                Student student = studentMap.get(it.getStudentId());
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
