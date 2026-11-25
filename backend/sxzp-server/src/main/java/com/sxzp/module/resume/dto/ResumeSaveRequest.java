package com.sxzp.module.resume.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 简历新增/修改参数。
 */
@Data
public class ResumeSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 为空表示新增 */
    private Long resumeId;

    @NotBlank(message = "简历名称不能为空")
    private String title;

    /** 是否可被企业浏览 */
    private Integer isPublic = 1;

    /** 是否设为默认投递简历 */
    private Integer isDefault = 0;

    /** 教育经历（JSON 字符串） */
    private String eduJson;

    private String skill;

    private String projectExp;

    private String selfEval;

    private String cont;
}
