package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 热门岗位排行项。
 */
@Data
public class JobRankingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobId;

    private String jobTitle;

    private String companyName;

    /** 投递人数 */
    private Long applyCount;

    /** 招聘人数 */
    private Integer headcount;

    /** 状态 */
    private String auditState;
}
