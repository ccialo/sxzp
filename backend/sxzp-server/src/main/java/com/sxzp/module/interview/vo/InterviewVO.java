package com.sxzp.module.interview.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试安排展示对象。
 */
@Data
public class InterviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long interviewId;

    private Long applyId;

    private Long jobId;

    private String jobTitle;

    private Long studentId;

    /** 学生姓名（企业视角） */
    private String studentName;

    private String studentNo;

    private String college;

    private Long companyId;

    private String companyName;

    private LocalDateTime interviewTime;

    private String place;

    private String contact;

    private String contactPhone;

    private String remark;

    /** pending/confirmed/cancelled/done */
    private String state;

    private LocalDateTime createTime;
}
