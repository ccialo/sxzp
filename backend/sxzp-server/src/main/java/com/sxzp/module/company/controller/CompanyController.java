package com.sxzp.module.company.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.company.dto.CompanySaveRequest;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业端接口：资料维护、查看自身档案。
 */
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /** 我的企业档案 */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Company> profile() {
        return Result.success(companyService.currentCompany());
    }

    /** 完善/修改企业资料 */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Company> saveProfile(@Validated @RequestBody CompanySaveRequest request) {
        return Result.success("保存成功", companyService.saveProfile(request));
    }

    /** 企业详情（登录用户可查看，用于岗位详情展示企业信息） */
    @GetMapping("/detail/{companyId}")
    public Result<Company> detail(@PathVariable Long companyId) {
        Company company = companyService.detail(companyId);
        company.setCreditCode(null);
        return Result.success(company);
    }
}
