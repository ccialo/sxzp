package com.sxzp.module.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业安排面试参数。
 */
@Data
public class InterviewSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联投递记录ID */
    @NotNull(message = "投递记录ID不能为空")
    private Long applyId;

    /**
     * 面试时间。
     * 前端 el-date-picker 以 "yyyy-MM-dd HH:mm:ss" 字符串提交，
     * 需显式声明格式，否则 LocalDateTime 默认只接受 ISO-8601（带 T 的形式）。
     */
    @NotNull(message = "面试时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewTime;

    /** 面试地点 */
    private String place;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String contactPhone;

    /** 备注 */
    private String remark;
}
