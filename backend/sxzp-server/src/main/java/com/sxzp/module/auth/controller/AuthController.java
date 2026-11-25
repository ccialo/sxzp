package com.sxzp.module.auth.controller;

import com.sxzp.common.result.Result;
import com.sxzp.common.security.LoginUser;
import com.sxzp.common.security.LoginUserContext;
import com.sxzp.module.auth.dto.LoginRequest;
import com.sxzp.module.auth.dto.RegisterRequest;
import com.sxzp.module.auth.service.AuthService;
import com.sxzp.module.auth.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录 / 注册 / 当前用户 / 退出。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    /** 注册（学生 / 企业） */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Validated @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<LoginUser> me() {
        return Result.success(LoginUserContext.require());
    }

    /** 退出登录：无状态 JWT 由前端清除令牌即可，这里仅返回成功 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        LoginUserContext.clear();
        return Result.success("已退出登录", null);
    }
}
