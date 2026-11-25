package com.sxzp.module.weekly.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.weekly.dto.WeeklyReportSaveRequest;
import com.sxzp.module.weekly.dto.WeeklyReviewRequest;
import com.sxzp.module.weekly.entity.WeeklyReport;
import com.sxzp.module.weekly.service.WeeklyReportService;
import com.sxzp.module.weekly.vo.WeeklyReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实习周报接口：学生提交、企业评价。
 */
@RestController
@RequestMapping("/api/weekly")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    /** 学生：提交周报 */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<WeeklyReport> submit(@Validated @RequestBody WeeklyReportSaveRequest request) {
        return Result.success("周报已提交", weeklyReportService.save(request));
    }

    /** 学生：我的周报列表 */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PageResult<WeeklyReportVO>> myReports(@RequestParam(defaultValue = "1") int current,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long applyId) {
        return Result.success(weeklyReportService.pageMine(current, size, applyId));
    }

    /** 企业：收到的周报列表 */
    @GetMapping("/received")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<PageResult<WeeklyReportVO>> received(@RequestParam(defaultValue = "1") int current,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) Long applyId,
                                                       @RequestParam(required = false) String state) {
        return Result.success(weeklyReportService.pageReceived(current, size, applyId, state));
    }

    /** 周报详情（学生/企业） */
    @GetMapping("/detail/{reportId}")
    public Result<WeeklyReportVO> detail(@PathVariable Long reportId) {
        return Result.success(weeklyReportService.detail(reportId));
    }

    /** 企业：评价周报（评分+评语） */
    @PostMapping("/review")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Void> review(@Validated @RequestBody WeeklyReviewRequest request) {
        weeklyReportService.review(request);
        return Result.success("评价已提交", null);
    }
}
