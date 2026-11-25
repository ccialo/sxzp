package com.sxzp.module.apply.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投递记录 job_apply。
 */
@Data
@TableName("job_apply")
public class JobApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "apply_id", type = IdType.AUTO)
    private Long applyId;

    private Long jobId;

    private Long studentId;

    private Long resumeId;

    /** 投递时的简历快照（JSON） */
    private String resumeSnapshot;

    /** pending/interview/agreement/hired/rejected/withdrawn */
    private String applyState;

    private LocalDateTime applyTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
