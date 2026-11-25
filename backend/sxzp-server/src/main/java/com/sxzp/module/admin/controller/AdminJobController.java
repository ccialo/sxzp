package com.sxzp.module.admin.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.job.dto.JobAuditRequest;
import com.sxzp.module.job.service.JobService;
import com.sxzp.module.job.vo.JobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 岗位审核接口。
 */
@RestController
@RequestMapping("/api/admin/job")
@RequiredArgsConstructor
public class AdminJobController {

    private final JobService jobService;

    /** 岗位列表（默认看待审核） */
    @GetMapping("/page")
    public Result<PageResult<JobVO>> page(@RequestParam(defaultValue = "1") int current,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String auditState,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(jobService.pageForAdmin(current, size, auditState, keyword));
    }

    /** 待审核岗位列表 */
    @GetMapping("/pending")
    public Result<PageResult<JobVO>> pending(@RequestParam(defaultValue = "1") int current,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(jobService.pageForAdmin(current, size, "pending", null));
    }

    /** 审核岗位 */
    @PostMapping("/audit")
    public Result<Void> audit(@Validated @RequestBody JobAuditRequest request) {
        jobService.audit(request);
        return Result.success("审核完成", null);
    }
}
