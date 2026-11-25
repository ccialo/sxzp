package com.sxzp.module.job.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 岗位检索条件（学生端）。
 */
@Data
public class JobQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关键词：岗位名称 / 企业名称 */
    private String keyword;

    /** 实习类型 */
    private String jobType;

    /** 工作地点 */
    private String workPlace;

    /** 岗位方向 */
    private String category;

    /** 最低津贴 */
    private BigDecimal minBenefit;

    /** 排序字段：publish_time / benefit_money */
    private String sortBy = "publish_time";

    /** 是否降序 */
    private Boolean desc = Boolean.TRUE;

    private Integer current = 1;

    private Integer size = 10;
}
