package com.sxzp.module.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sxzp.common.exception.BizException;
import com.sxzp.common.security.JwtUtils;
import com.sxzp.module.auth.dto.LoginRequest;
import com.sxzp.module.auth.dto.RegisterRequest;
import com.sxzp.module.auth.vo.LoginResponse;
import com.sxzp.module.company.entity.Company;
import com.sxzp.module.company.mapper.CompanyMapper;
import com.sxzp.module.student.entity.Student;
import com.sxzp.module.student.mapper.StudentMapper;
import com.sxzp.module.user.entity.SysUser;
import com.sxzp.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务：登录、注册。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final StudentMapper studentMapper;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 账号登录。
     */
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getAccount, request.getAccount()));
        if (user == null) {
            throw new BizException("账号或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        if (!"normal".equals(user.getStatus())) {
            throw new BizException("账号已被禁用，请联系管理员");
        }

        String token = jwtUtils.createToken(user.getUserId(), user.getAccount(), user.getUserType());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setAccount(user.getAccount());
        response.setUserType(user.getUserType());
        response.setRealName(user.getRealName());
        response.setProfileId(resolveProfileId(user));
        log.info("用户登录成功: account={}, type={}", user.getAccount(), user.getUserType());
        return response;
    }

    /**
     * 注册（学生 / 企业）。管理员账号不开放自助注册。
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        String userType = request.getUserType();
        if (!"student".equals(userType) && !"company".equals(userType)) {
            throw new BizException("仅支持学生或企业账号注册");
        }
        Long exists = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getAccount, request.getAccount()));
        if (exists != null && exists > 0) {
            throw new BizException("账号已存在，请更换账号");
        }

        SysUser user = new SysUser();
        user.setAccount(request.getAccount());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(userType);
        user.setStatus("normal");
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        sysUserMapper.insert(user);

        if ("student".equals(userType)) {
            Student student = new Student();
            student.setUserOwner(user.getUserId());
            student.setRealName(request.getRealName());
            student.setStudentNo(request.getStudentNo() == null ? request.getAccount() : request.getStudentNo());
            student.setCollege(request.getCollege());
            student.setMajor(request.getMajor());
            student.setGradeYear(request.getGradeYear());
            studentMapper.insert(student);
        } else {
            Company company = new Company();
            company.setUserOwner(user.getUserId());
            company.setEntName(request.getEntName() == null ? request.getAccount() : request.getEntName());
            company.setCreditCode(request.getCreditCode());
            company.setCategory(request.getCategory());
            company.setContactName(request.getRealName());
            company.setContactEmail(request.getEmail());
            company.setContactPhone(request.getPhone());
            company.setAddress(request.getAddress());
            // 企业注册后须经学校管理员审核
            company.setAuthState("pending");
            companyMapper.insert(company);
        }

        log.info("新用户注册成功: account={}, type={}", user.getAccount(), userType);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount(request.getAccount());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    private Long resolveProfileId(SysUser user) {
        if ("student".equals(user.getUserType())) {
            Student student = studentMapper.selectOne(
                    Wrappers.<Student>lambdaQuery().eq(Student::getUserOwner, user.getUserId()));
            return student == null ? null : student.getStudentId();
        }
        if ("company".equals(user.getUserType())) {
            Company company = companyMapper.selectOne(
                    Wrappers.<Company>lambdaQuery().eq(Company::getUserOwner, user.getUserId()));
            return company == null ? null : company.getCompanyId();
        }
        return null;
    }
}
