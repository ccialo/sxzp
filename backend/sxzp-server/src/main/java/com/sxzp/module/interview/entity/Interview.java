package com.sxzp.module.interview.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试安排 interview。
 */
@Data
@TableName("interview")
public class Interview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "interview_id", type = IdType.AUTO)
    private Long interviewId;

    /** 关联投递记录 */
    private Long applyId;

    private Long jobId;

    private Long studentId;

    private Long companyId;

    /** 面试时间 */
    private LocalDateTime interviewTime;

    /** 面试地点 */
    private String place;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String contactPhone;

    /** 备注 */
    private String remark;

    /** pending(待确认)/confirmed(已确认)/cancelled(已取消)/done(已完成) */
    private String state;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
