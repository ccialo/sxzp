package com.sxzp.module.admin.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.company.dto.CompanyAuditRequest;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 企业认证审核接口。
 */
@RestController
@RequestMapping("/api/admin/company")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyService companyService;

    /** 待审核企业列表 */
    @GetMapping("/pending")
    public Result<PageResult<Company>> pending(@RequestParam(defaultValue = "1") int current,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String keyword) {
        return Result.success(companyService.pageAudit(current, size, keyword));
    }

    /** 企业列表（可按认证状态筛选） */
    @GetMapping("/page")
    public Result<PageResult<Company>> page(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String authState,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(companyService.page(current, size, authState, keyword));
    }

    /** 审核企业认证 */
    @PostMapping("/audit")
    public Result<Void> audit(@Validated @RequestBody CompanyAuditRequest request) {
        companyService.audit(request);
        return Result.success("审核完成", null);
    }
}
