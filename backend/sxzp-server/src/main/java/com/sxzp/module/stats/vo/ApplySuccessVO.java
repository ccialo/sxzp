package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学生投递成功率总览（整体 + 按岗位类型）。
 */
@Data
public class ApplySuccessVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 投递总数 */
    private Long total;

    /** 成功投递数（进入面试 / 待协议 / 已录用） */
    private Long success;

    /** 整体成功率，百分比值（如 24.4 表示 24.4%） */
    private Double rate;

    /** 按岗位类型细分 */
    private List<JobTypeSuccessVO> jobTypeRates;
}
