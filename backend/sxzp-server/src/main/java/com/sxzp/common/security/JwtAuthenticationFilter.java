package com.sxzp.common.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器：解析请求头令牌，写入 SecurityContext 与登录用户上下文。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String headerValue = request.getHeader(jwtUtils.getHeader());
        String token = jwtUtils.resolveToken(headerValue);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtUtils.parse(token);
            if (claims != null) {
                Long userId = claims.get("userId", Number.class) == null
                        ? null : claims.get("userId", Number.class).longValue();
                String account = claims.get("account", String.class);
                String userType = claims.get("userType", String.class);
                LoginUser loginUser = new LoginUser(userId, account, userType);
                LoginUserContext.set(loginUser);
                // 以 ROLE_xxx 形式授予权限，供 @PreAuthorize / hasRole 使用
                String role = userType == null ? "UNKNOWN" : userType.toUpperCase();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        loginUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            LoginUserContext.clear();
        }
    }
}
