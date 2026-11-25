package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 管理端仪表盘总览统计。
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 基础数据 =====
    /** 学生总数 */
    private Long studentCount;
    /** 企业总数 */
    private Long companyCount;
    /** 认证通过企业数 */
    private Long companyAuthedCount;
    /** 岗位总数 */
    private Long jobCount;
    /** 在线岗位数 */
    private Long jobOnlineCount;

    // ===== 投递统计 =====
    /** 投递总数 */
    private Long applyCount;
    /** 各状态投递数 */
    private Map<String, Long> applyStateCount;
    /** 今日投递数 */
    private Long todayApplyCount;

    // ===== 面试统计 =====
    /** 面试安排总数 */
    private Long interviewCount;
    /** 各状态面试数 */
    private Map<String, Long> interviewStateCount;

    // ===== 协议统计 =====
    /** 协议总数 */
    private Long agreementCount;
    /** 已签署协议数 */
    private Long agreementSignedCount;

    // ===== 周报统计 =====
    /** 周报总数 */
    private Long weeklyReportCount;
    /** 已评价周报数 */
    private Long weeklyReviewedCount;
}
