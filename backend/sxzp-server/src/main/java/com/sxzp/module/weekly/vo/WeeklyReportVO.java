package com.sxzp.module.weekly.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习周报展示对象。
 */
@Data
public class WeeklyReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reportId;

    private Long applyId;

    private Long jobId;

    private String jobTitle;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private Long companyId;

    private String companyName;

    private Integer weekNum;

    private LocalDate startDate;

    private LocalDate endDate;

    private String content;

    private String plan;

    private String problem;

    /** 企业评分 1-5 */
    private Integer reviewScore;

    /** 企业评语 */
    private String reviewComment;

    /** submitted/reviewed */
    private String state;

    private LocalDateTime createTime;
}
