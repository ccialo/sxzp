package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 按岗位类型统计的投递成功率。
 */
@Data
public class JobTypeSuccessVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 岗位类型代码（teaching/professional/graduation） */
    private String jobType;

    /** 该类型投递总数 */
    private Long total;

    /** 该类型成功投递数（进入面试及之后环节） */
    private Long success;

    /** 成功率，百分比值（如 31.5 表示 31.5%） */
    private Double rate;
}
