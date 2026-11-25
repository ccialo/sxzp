package com.sxzp.module.weekly.service;

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
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.mapper.JobMapper;
import com.sxzp.module.notice.service.NoticeService;
import com.sxzp.module.resume.service.ResumeService;
import com.sxzp.module.student.entity.Student;
import com.sxzp.module.student.mapper.StudentMapper;
import com.sxzp.module.weekly.dto.WeeklyReportSaveRequest;
import com.sxzp.module.weekly.dto.WeeklyReviewRequest;
import com.sxzp.module.weekly.entity.WeeklyReport;
import com.sxzp.module.weekly.mapper.WeeklyReportMapper;
import com.sxzp.module.weekly.vo.WeeklyReportVO;
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
 * 实习周报服务：学生提交、企业评价。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    public static final String STATE_SUBMITTED = "submitted";
    public static final String STATE_REVIEWED = "reviewed";

    private final WeeklyReportMapper reportMapper;
    private final JobApplyMapper applyMapper;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final StudentMapper studentMapper;
    private final CompanyService companyService;
    private final ResumeService resumeService;
    private final NoticeService noticeService;

    /** 学生提交周报 */
    @Transactional(rollbackFor = Exception.class)
    public WeeklyReport save(WeeklyReportSaveRequest request) {
        JobApply apply = applyMapper.selectById(request.getApplyId());
        if (apply == null) {
            throw new BizException(404, "投递记录不存在");
        }
        Student student = resumeService.currentStudent();
        if (!student.getStudentId().equals(apply.getStudentId())) {
            throw new BizException(403, "无权为他人投递提交周报");
        }
        // 同一投递同一周次不可重复提交
        Long dup = reportMapper.selectCount(Wrappers.<WeeklyReport>lambdaQuery()
                .eq(WeeklyReport::getApplyId, apply.getApplyId())
                .eq(WeeklyReport::getWeekNum, request.getWeekNum()));
        if (dup != null && dup > 0) {
            throw new BizException("第 " + request.getWeekNum() + " 周周报已提交，请勿重复提交");
        }

        WeeklyReport report = new WeeklyReport();
        report.setApplyId(apply.getApplyId());
        report.setJobId(apply.getJobId());
        report.setStudentId(student.getStudentId());
        report.setCompanyId(resolveCompanyId(apply.getJobId()));
        report.setWeekNum(request.getWeekNum());
        report.setStartDate(request.getStartDate());
        report.setEndDate(request.getEndDate());
        report.setContent(request.getContent());
        report.setPlan(request.getPlan());
        report.setProblem(request.getProblem());
        report.setState(STATE_SUBMITTED);
        reportMapper.insert(report);

        // 通知企业
        Long companyId = report.getCompanyId();
        if (companyId != null) {
            Company company = companyMapper.selectById(companyId);
            if (company != null) {
                noticeService.send(company.getUserOwner(), "新周报待评价",
                        "学生提交了第 " + request.getWeekNum() + " 周实习周报，请及时评价。",
                        NoticeService.TYPE_SYSTEM);
            }
        }
        log.info("周报提交成功: reportId={}, applyId={}, weekNum={}",
                report.getReportId(), apply.getApplyId(), request.getWeekNum());
        return report;
    }

    /** 学生：我的周报列表 */
    public PageResult<WeeklyReportVO> pageMine(int current, int size, Long applyId) {
        Student student = resumeService.currentStudent();
        LambdaQueryWrapper<WeeklyReport> wrapper = Wrappers.<WeeklyReport>lambdaQuery()
                .eq(WeeklyReport::getStudentId, student.getStudentId())
                .orderByDesc(WeeklyReport::getWeekNum)
                .orderByDesc(WeeklyReport::getCreateTime);
        if (applyId != null) {
            wrapper.eq(WeeklyReport::getApplyId, applyId);
        }
        Page<WeeklyReport> page = reportMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toVOList(page.getRecords(), true), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业：收到的周报列表（可按投递筛选） */
    public PageResult<WeeklyReportVO> pageReceived(int current, int size, Long applyId, String state) {
        Long companyId = companyService.currentCompanyId();
        LambdaQueryWrapper<WeeklyReport> wrapper = Wrappers.<WeeklyReport>lambdaQuery()
                .eq(WeeklyReport::getCompanyId, companyId)
                .orderByDesc(WeeklyReport::getCreateTime);
        if (applyId != null) {
            wrapper.eq(WeeklyReport::getApplyId, applyId);
        }
        if (StringUtils.isNotBlank(state)) {
            wrapper.eq(WeeklyReport::getState, state);
        }
        Page<WeeklyReport> page = reportMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(toVOList(page.getRecords(), false), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 周报详情 */
    public WeeklyReportVO detail(Long reportId) {
        WeeklyReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BizException(404, "周报不存在");
        }
        // 归属校验：管理员可查看所有，学生只能看自己的，企业只能看自己学生的
        LoginUser loginUser = LoginUserContext.require();
        if (loginUser.isAdmin()) {
            // 管理员无限制
        } else if (loginUser.isStudent()) {
            Student student = resumeService.currentStudent();
            if (!student.getStudentId().equals(report.getStudentId())) {
                throw new BizException(403, "无权查看该周报");
            }
        } else if (loginUser.isCompany()) {
            Long companyId = companyService.currentCompanyId();
            if (!companyId.equals(report.getCompanyId())) {
                throw new BizException(403, "无权查看该周报");
            }
        } else {
            throw new BizException(403, "无权查看该周报");
        }
        List<WeeklyReportVO> list = toVOList(Collections.singletonList(report), false);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 企业评价周报：评分+评语 */
    @Transactional(rollbackFor = Exception.class)
    public void review(WeeklyReviewRequest request) {
        WeeklyReport report = reportMapper.selectById(request.getReportId());
        if (report == null) {
            throw new BizException(404, "周报不存在");
        }
        Long companyId = companyService.currentCompanyId();
        if (!companyId.equals(report.getCompanyId())) {
            throw new BizException(403, "无权评价该周报");
        }
        report.setReviewScore(request.getScore());
        report.setReviewComment(request.getComment());
        report.setState(STATE_REVIEWED);
        reportMapper.updateById(report);
        // 通知学生
        Student student = studentMapper.selectById(report.getStudentId());
        if (student != null) {
            noticeService.send(student.getUserOwner(), "周报已评价",
                    "您的第 " + report.getWeekNum() + " 周周报已被企业评价。",
                    NoticeService.TYPE_SYSTEM);
        }
        log.info("周报评价完成: reportId={}, score={}", report.getReportId(), request.getScore());
    }

    private Long resolveCompanyId(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        return job == null ? null : job.getCompanyId();
    }

    private List<WeeklyReportVO> toVOList(List<WeeklyReport> reports, boolean forStudent) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> jobIds = reports.stream().map(WeeklyReport::getJobId).distinct().collect(Collectors.toList());
        Map<Long, Job> jobMap = jobIds.isEmpty() ? Collections.emptyMap()
                : jobMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(Job::getJobId, Function.identity(), (a, b) -> a));

        List<Long> companyIds = reports.stream().map(WeeklyReport::getCompanyId).distinct().collect(Collectors.toList());
        Map<Long, Company> companyMap = companyIds.isEmpty() ? Collections.emptyMap()
                : companyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(Company::getCompanyId, Function.identity(), (a, b) -> a));

        List<Long> studentIds = reports.stream().map(WeeklyReport::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, Student> studentMap = studentIds.isEmpty() ? Collections.emptyMap()
                : studentMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity(), (a, b) -> a));

        List<WeeklyReportVO> list = new ArrayList<>(reports.size());
        for (WeeklyReport r : reports) {
            WeeklyReportVO vo = new WeeklyReportVO();
            vo.setReportId(r.getReportId());
            vo.setApplyId(r.getApplyId());
            vo.setJobId(r.getJobId());
            vo.setStudentId(r.getStudentId());
            vo.setCompanyId(r.getCompanyId());
            vo.setWeekNum(r.getWeekNum());
            vo.setStartDate(r.getStartDate());
            vo.setEndDate(r.getEndDate());
            vo.setContent(r.getContent());
            vo.setPlan(r.getPlan());
            vo.setProblem(r.getProblem());
            vo.setReviewScore(r.getReviewScore());
            vo.setReviewComment(r.getReviewComment());
            vo.setState(r.getState());
            vo.setCreateTime(r.getCreateTime());
            Job job = jobMap.get(r.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobTitle());
            }
            Company company = companyMap.get(r.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getEntName());
            }
            if (!forStudent) {
                Student student = studentMap.get(r.getStudentId());
                if (student != null) {
                    vo.setStudentName(student.getRealName());
                    vo.setStudentNo(student.getStudentNo());
                }
            }
            list.add(vo);
        }
        return list;
    }
}
