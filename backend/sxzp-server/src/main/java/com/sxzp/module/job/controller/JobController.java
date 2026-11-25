package com.sxzp.module.job.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.job.dto.JobQueryRequest;
import com.sxzp.module.job.dto.JobSaveRequest;
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.service.JobService;
import com.sxzp.module.job.vo.JobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位接口：学生端检索/详情 + 企业端发布维护。
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /** 岗位检索（允许匿名访问） */
    @PostMapping("/search")
    public Result<PageResult<JobVO>> search(@RequestBody JobQueryRequest query) {
        return Result.success(jobService.search(query));
    }

    /** 岗位详情（允许匿名访问） */
    @GetMapping("/detail/{jobId}")
    public Result<JobVO> detail(@PathVariable Long jobId) {
        return Result.success(jobService.detail(jobId));
    }

    /** 企业发布/修改岗位 */
    @PostMapping("/save")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Job> save(@Validated @RequestBody JobSaveRequest request) {
        return Result.success("保存成功", jobService.save(request));
    }

    /** 企业：我的岗位列表 */
    @GetMapping("/my")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<PageResult<JobVO>> myJobs(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String auditState) {
        return Result.success(jobService.pageMyJobs(current, size, auditState));
    }

    /** 企业下线岗位 */
    @PutMapping("/offline/{jobId}")
    @PreAuthorize("hasRole('COMPANY') or hasRole('ADMIN')")
    public Result<Void> offline(@PathVariable Long jobId) {
        jobService.offline(jobId);
        return Result.success("岗位已下线", null);
    }

    /** 企业重新提交岗位审核 */
    @PutMapping("/resubmit/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> resubmit(@PathVariable Long jobId) {
        jobService.resubmit(jobId);
        return Result.success("已提交审核", null);
    }
}
