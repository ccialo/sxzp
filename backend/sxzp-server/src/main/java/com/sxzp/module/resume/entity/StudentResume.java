package com.sxzp.module.resume.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生简历 student_resume。
 */
@Data
@TableName("student_resume")
public class StudentResume implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "resume_id", type = IdType.AUTO)
    private Long resumeId;

    private Long studentId;

    /** 简历名称，如“软件工程-张同学” */
    private String title;

    /** 是否可被企业浏览 */
    private Integer isPublic;

    /** 是否为默认投递简历 */
    private Integer isDefault;

    /** 教育经历（JSON 字符串） */
    private String eduJson;

    /** 技能与证书 */
    private String skill;

    /** 项目/实践经历 */
    private String projectExp;

    /** 自我评价 */
    private String selfEval;

    /** 完整简历正文 */
    private String cont;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
