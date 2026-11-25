package com.sxzp.module.stats.controller;

import com.sxzp.common.result.Result;
import com.sxzp.module.stats.service.StatsService;
import com.sxzp.module.stats.vo.ApplySuccessVO;
import com.sxzp.module.stats.vo.ApplyTrendVO;
import com.sxzp.module.stats.vo.DashboardVO;
import com.sxzp.module.stats.vo.JobRankingVO;
import com.sxzp.module.stats.vo.NameCountVO;
import com.sxzp.module.stats.vo.RecentApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计报表接口（管理端）。
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 仪表盘总览 */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DashboardVO> dashboard() {
        return Result.success(statsService.dashboard());
    }

    /** 热门岗位排行（默认前 10） */
    @GetMapping("/job-ranking")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<JobRankingVO>> jobRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.jobRanking(limit));
    }

    /** 投递趋势（默认近 7 天） */
    @GetMapping("/apply-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<ApplyTrendVO>> applyTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.applyTrend(days));
    }

    /** 岗位类型分布（各类型岗位数） */
    @GetMapping("/job-type-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<NameCountVO>> jobTypeDistribution() {
        return Result.success(statsService.jobTypeDistribution());
    }

    /** 学生学院分布（默认前 10 个学院） */
    @GetMapping("/college-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<NameCountVO>> collegeDistribution(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.collegeDistribution(limit));
    }

    /** 企业认证状态分布 */
    @GetMapping("/company-auth-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<NameCountVO>> companyAuthDistribution() {
        return Result.success(statsService.companyAuthDistribution());
    }

    /** 学生投递成功率（整体 + 按岗位类型） */
    @GetMapping("/apply-success")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApplySuccessVO> applySuccess() {
        return Result.success(statsService.applySuccess());
    }

    /** 最近投递动态（默认 8 条） */
    @GetMapping("/recent-applies")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RecentApplyVO>> recentApplies(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(statsService.recentApplies(limit));
    }
}
