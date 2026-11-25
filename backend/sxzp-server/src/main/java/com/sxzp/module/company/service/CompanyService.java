package com.sxzp.module.company.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.result.PageResult;
import com.sxzp.common.security.LoginUser;
import com.sxzp.common.security.LoginUserContext;
import com.sxzp.module.company.dto.CompanyAuditRequest;
import com.sxzp.module.company.dto.CompanySaveRequest;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.mapper.CompanyMapper;
import com.sxzp.module.job.entity.Job;
import com.sxzp.module.job.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 企业服务：资料维护、认证审核、查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyMapper companyMapper;
    private final JobMapper jobMapper;

    /** 当前登录企业（企业用户调用） */
    public Company currentCompany() {
        LoginUser loginUser = LoginUserContext.require();
        Company company = companyMapper.selectOne(
                Wrappers.<Company>lambdaQuery().eq(Company::getUserOwner, loginUser.getUserId()));
        if (company == null) {
            throw new BizException(404, "未找到企业档案，请先完善企业资料");
        }
        return company;
    }

    /** 当前登录企业的ID */
    public Long currentCompanyId() {
        return currentCompany().getCompanyId();
    }

    /** 查询企业详情 */
    public Company detail(Long companyId) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new BizException(404, "企业不存在");
        }
        return company;
    }

    /** 完善/修改企业资料（企业用户） */
    @Transactional(rollbackFor = Exception.class)
    public Company saveProfile(CompanySaveRequest request) {
        Company company = currentCompany();
        // 单位名称唯一性校验
        Long dup = companyMapper.selectCount(Wrappers.<Company>lambdaQuery()
                .eq(Company::getEntName, request.getEntName())
                .ne(Company::getCompanyId, company.getCompanyId()));
        if (dup != null && dup > 0) {
            throw new BizException("该单位名称已被注册");
        }
        boolean nameChanged = !StringUtils.equals(company.getEntName(), request.getEntName());
        company.setEntName(request.getEntName());
        company.setCreditCode(request.getCreditCode());
        company.setCategory(request.getCategory());
        company.setContactName(request.getContactName());
        company.setContactPhone(request.getContactPhone());
        company.setContactEmail(request.getContactEmail());
        company.setAddress(request.getAddress());
        company.setIntro(request.getIntro());
        if (StringUtils.isNotBlank(request.getCertFile())) {
            company.setCertFile(request.getCertFile());
        }
        // 关键资料变更后需要重新认证
        if (nameChanged || StringUtils.isNotBlank(company.getRejectReason())) {
            company.setAuthState("pending");
            company.setRejectReason(null);
        }
        companyMapper.updateById(company);
        log.info("企业资料更新: companyId={}", company.getCompanyId());
        return company;
    }

    /** 管理员审核企业认证 */
    @Transactional(rollbackFor = Exception.class)
    public void audit(CompanyAuditRequest request) {
        Company company = companyMapper.selectById(request.getCompanyId());
        if (company == null) {
            throw new BizException(404, "企业不存在");
        }
        if (Boolean.TRUE.equals(request.getPass())) {
            company.setAuthState("auth");
            company.setRejectReason(null);
        } else {
            company.setAuthState("reject");
            company.setRejectReason(StringUtils.defaultIfBlank(request.getOpinion(), "资料不符合要求"));
        }
        companyMapper.updateById(company);
        log.info("企业审核完成: companyId={}, state={}", company.getCompanyId(), company.getAuthState());
    }

    /** 待审核企业列表（管理员） */
    public PageResult<Company> pageAudit(int current, int size, String keyword) {
        LambdaQueryWrapper<Company> wrapper = Wrappers.<Company>lambdaQuery()
                .eq(Company::getAuthState, "pending")
                .orderByDesc(Company::getCreateTime);
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Company::getEntName, keyword);
        }
        Page<Company> page = companyMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业列表（管理员，可按状态/关键字筛选） */
    public PageResult<Company> page(int current, int size, String authState, String keyword) {
        LambdaQueryWrapper<Company> wrapper = Wrappers.<Company>lambdaQuery()
                .orderByDesc(Company::getCreateTime);
        if (StringUtils.isNotBlank(authState)) {
            wrapper.eq(Company::getAuthState, authState);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Company::getEntName, keyword);
        }
        Page<Company> page = companyMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 企业是否已认证（发布岗位前置校验） */
    public void assertAuthenticated(Long companyId) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new BizException(404, "企业不存在");
        }
        if (!"auth".equals(company.getAuthState())) {
            throw new BizException("企业尚未通过认证，暂不能发布岗位");
        }
    }

    /** 企业名下岗位数 */
    public long countJobs(Long companyId) {
        Long count = jobMapper.selectCount(Wrappers.<Job>lambdaQuery().eq(Job::getCompanyId, companyId));
        return count == null ? 0L : count;
    }

    /** 批量统计企业id → 岗位数 */
    public List<Job> listJobsOfCompany(Long companyId) {
        return jobMapper.selectList(Wrappers.<Job>lambdaQuery().eq(Job::getCompanyId, companyId));
    }
}
