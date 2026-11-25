package com.sxzp.module.stats.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 最近投递动态（管理端数据分析用）。
 */
@Data
public class RecentApplyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long applyId;

    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 学院 */
    private String college;

    /** 投递岗位 */
    private String jobTitle;

    /** 企业名称 */
    private String companyName;

    /** 投递状态 */
    private String applyState;

    /** 投递时间 */
    private LocalDateTime applyTime;
}
