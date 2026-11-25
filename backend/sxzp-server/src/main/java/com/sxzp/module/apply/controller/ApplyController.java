package com.sxzp.module.apply.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.apply.dto.ApplyAuditRequest;
import com.sxzp.module.apply.dto.ApplySubmitRequest;
import com.sxzp.module.apply.entity.JobApply;
import com.sxzp.module.apply.service.ApplyService;
import com.sxzp.module.apply.vo.ApplyVO;
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
 * 投递接口：学生投递/查询，企业筛选处理。
 */
@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;

    /** 学生投递简历 */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<JobApply> submit(@Validated @RequestBody ApplySubmitRequest request) {
        return Result.success("投递成功", applyService.submit(request));
    }

    /** 学生撤回投递 */
    @PutMapping("/withdraw/{applyId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> withdraw(@PathVariable Long applyId) {
        applyService.withdraw(applyId);
        return Result.success("已撤回投递", null);
    }

    /** 学生：我的投递列表 */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PageResult<ApplyVO>> myApplies(@RequestParam(defaultValue = "1") int current,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String applyState) {
        return Result.success(applyService.pageMyApplies(current, size, applyState));
    }

    /** 企业：收到的投递列表 */
    @GetMapping("/received")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<PageResult<ApplyVO>> received(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Long jobId,
                                                @RequestParam(required = false) String applyState) {
        return Result.success(applyService.pageReceived(current, size, jobId, applyState));
    }

    /** 企业：处理投递（进入面试 / 淘汰） */
    @PostMapping("/audit")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> audit(@Validated @RequestBody ApplyAuditRequest request) {
        applyService.audit(request);
        return Result.success("处理完成", null);
    }
}
