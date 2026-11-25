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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开统计接口：供数据大屏使用，无需登录即可访问。
 * 仅暴露聚合统计信息；最近投递中的学生姓名、学号做脱敏处理。
 */
@RestController
@RequestMapping("/api/public/stats")
@RequiredArgsConstructor
public class PublicStatsController {

    private final StatsService statsService;

    /** 大屏总览指标 */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(statsService.dashboard());
    }

    /** 投递趋势（默认近 7 天） */
    @GetMapping("/apply-trend")
    public Result<List<ApplyTrendVO>> applyTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.applyTrend(days));
    }

    /** 热门岗位排行（默认前 10） */
    @GetMapping("/job-ranking")
    public Result<List<JobRankingVO>> jobRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.jobRanking(limit));
    }

    /** 岗位类型分布 */
    @GetMapping("/job-type-distribution")
    public Result<List<NameCountVO>> jobTypeDistribution() {
        return Result.success(statsService.jobTypeDistribution());
    }

    /** 学生学院分布（默认前 10） */
    @GetMapping("/college-distribution")
    public Result<List<NameCountVO>> collegeDistribution(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statsService.collegeDistribution(limit));
    }

    /** 企业认证状态分布 */
    @GetMapping("/company-auth-distribution")
    public Result<List<NameCountVO>> companyAuthDistribution() {
        return Result.success(statsService.companyAuthDistribution());
    }

    /** 学生投递成功率（整体 + 按岗位类型） */
    @GetMapping("/apply-success")
    public Result<ApplySuccessVO> applySuccess() {
        return Result.success(statsService.applySuccess());
    }

    /** 最近投递动态（学生姓名、学号脱敏） */
    @GetMapping("/recent-applies")
    public Result<List<RecentApplyVO>> recentApplies(@RequestParam(defaultValue = "8") int limit) {
        List<RecentApplyVO> list = statsService.recentApplies(limit);
        for (RecentApplyVO vo : list) {
            vo.setStudentName(maskName(vo.getStudentName()));
            vo.setStudentNo(maskNo(vo.getStudentNo()));
        }
        return Result.success(list);
    }

    /** 姓名脱敏：保留姓氏，其余用 * 代替 */
    private static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name + "*";
        }
        return name.charAt(0) + repeat("*", name.length() - 1);
    }

    /** 学号脱敏：保留前 4 位 */
    private static String maskNo(String no) {
        if (no == null || no.length() <= 4) {
            return no;
        }
        return no.substring(0, 4) + repeat("*", no.length() - 4);
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
