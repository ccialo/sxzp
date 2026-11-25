package com.sxzp.module.agreement.controller;

import com.sxzp.common.result.PageResult;
import com.sxzp.common.result.Result;
import com.sxzp.module.agreement.dto.AgreementSaveRequest;
import com.sxzp.module.agreement.entity.Agreement;
import com.sxzp.module.agreement.service.AgreementService;
import com.sxzp.module.agreement.vo.AgreementVO;
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
 * 实习协议接口：企业发起、学生签署/拒绝。
 */
@RestController
@RequestMapping("/api/agreement")
@RequiredArgsConstructor
public class AgreementController {

    private final AgreementService agreementService;

    /** 企业：发起实习协议 */
    @PostMapping("/save")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<Agreement> save(@Validated @RequestBody AgreementSaveRequest request) {
        return Result.success("协议已发起", agreementService.save(request));
    }

    /** 学生：我的协议列表 */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<PageResult<AgreementVO>> myAgreements(@RequestParam(defaultValue = "1") int current,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String state) {
        return Result.success(agreementService.pageMine(current, size, state));
    }

    /** 企业：我发出的协议列表 */
    @GetMapping("/sent")
    @PreAuthorize("hasRole('COMPANY')")
    public Result<PageResult<AgreementVO>> sentAgreements(@RequestParam(defaultValue = "1") int current,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String state) {
        return Result.success(agreementService.pageSent(current, size, state));
    }

    /** 学生：签署协议 */
    @PutMapping("/sign/{agreementId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> sign(@PathVariable Long agreementId) {
        agreementService.sign(agreementId);
        return Result.success("协议已签署", null);
    }

    /** 学生：拒绝协议 */
    @PutMapping("/reject/{agreementId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> reject(@PathVariable Long agreementId) {
        agreementService.reject(agreementId);
        return Result.success("已拒绝协议", null);
    }
}
