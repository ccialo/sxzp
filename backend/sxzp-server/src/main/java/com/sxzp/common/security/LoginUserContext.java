package com.sxzp.common.security;

import com.sxzp.common.exception.BizException;

/**
 * 登录用户上下文：通过 ThreadLocal 保存当前请求的登录用户。
 */
public final class LoginUserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录则抛出 401 业务异常。
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BizException(401, "未登录或登录已过期，请重新登录");
        }
        return user;
    }

    public static Long requireUserId() {
        return require().getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
