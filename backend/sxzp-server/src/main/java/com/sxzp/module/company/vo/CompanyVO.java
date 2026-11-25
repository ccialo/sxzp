package com.sxzp.module.company.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业信息展示对象。
 */
@Data
public class CompanyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long companyId;

    private Long userOwner;

    private String entName;

    private String creditCode;

    private String category;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String intro;

    private String certFile;

    private String authState;

    private String rejectReason;

    private LocalDateTime createTime;

    /** 在招岗位数量（列表展示用，可能为 null） */
    private Long onlineJobCount;
}
