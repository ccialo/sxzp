package com.sxzp.module.company.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 企业资料提交/修改参数。
 */
@Data
public class CompanySaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "单位名称不能为空")
    private String entName;

    private String creditCode;

    private String category;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String intro;

    /** 资质附件路径 */
    private String certFile;
}
