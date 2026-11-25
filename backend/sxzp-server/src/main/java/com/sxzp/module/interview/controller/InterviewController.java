package com.sxzp.module.interview.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.interview.dto.InterviewSaveRequest;
import com.sxzp.module.interview.entity.Interview;
import com.sxzp.module.interview.service.InterviewService;
import com.sxzp.module.interview.vo.InterviewVO;
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
 * 面试安排接口：企业发起、学生确认/取消、企业完成。
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** 企业：安排面试 */
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Interview> schedule(@Validated @RequestBody InterviewSaveRequest request) {
        return Result.success("面试已安排", interviewService.schedule(request));
    }

    /** 学生：我的面试列表 */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PageResult<InterviewVO>> myInterviews(@RequestParam(defaultValue = "1") int current,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String state) {
        return Result.success(interviewService.pageMyInterviews(current, size, state));
    }

    /** 企业：我发出的面试列表 */
    @GetMapping("/sent")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<PageResult<InterviewVO>> sentInterviews(@RequestParam(defaultValue = "1") int current,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String state) {
        return Result.success(interviewService.pageSentInterviews(current, size, state));
    }

    /** 学生：确认面试 */
    @PutMapping("/confirm/{interviewId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> confirm(@PathVariable Long interviewId) {
        interviewService.confirm(interviewId);
        return Result.success("已确认面试", null);
    }

    /** 取消面试（学生/企业均可） */
    @PutMapping("/cancel/{interviewId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('COMPANY')")
    public Result<Void> cancel(@PathVariable Long interviewId) {
        interviewService.cancel(interviewId);
        return Result.success("已取消面试", null);
    }

    /** 企业：标记面试完成 */
    @PutMapping("/done/{interviewId}")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> done(@PathVariable Long interviewId) {
        interviewService.done(interviewId);
        return Result.success("面试已完成", null);
    }
}
