package com.sxzp.module.apply.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投递记录展示对象（学生/企业双视角）。
 */
@Data
public class ApplyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long applyId;

    private Long jobId;

    private String jobTitle;

    private String workPlace;

    private String jobType;

    private Long companyId;

    private String companyName;

    private Long studentId;

    /** 学生姓名（企业视角） */
    private String studentName;

    private String studentNo;

    private String college;

    private String major;

    private Long resumeId;

    private String resumeTitle;

    private String applyState;

    private LocalDateTime applyTime;

    /** 投递时的简历快照（企业查看时返回） */
    private String resumeSnapshot;
}
