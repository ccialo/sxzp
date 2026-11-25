package com.sxzp.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sxzp.common.result.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security 配置：无状态 JWT 认证 + 基于角色的接口访问控制 + 跨域。
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /** 口令加密器：BCrypt */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 放行：登录/注册、错误页、静态资源、接口文档、上传文件
                .antMatchers("/api/auth/login", "/api/auth/register", "/api/auth/captcha").permitAll()
                .antMatchers("/", "/index.html", "/static/**", "/favicon.ico", "/error").permitAll()
                .antMatchers("/api/doc", "/uploads/**", "/api/dict/**").permitAll()
                // 数据大屏公开统计接口（无需登录）
                .antMatchers("/api/public/**").permitAll()
                // 岗位检索/详情、企业详情允许匿名浏览（未登录也可查看）
                .antMatchers("/api/job/search", "/api/job/detail/**").permitAll()
                .antMatchers(org.springframework.http.HttpMethod.GET, "/api/company/detail/**").permitAll()
                // 管理端接口
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // 其余接口需登录
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, ex) -> writeJson(response, HttpStatus.UNAUTHORIZED,
                        Result.fail(401, "未认证或登录已过期，请重新登录")))
                .accessDeniedHandler((request, response, ex) -> writeJson(response, HttpStatus.FORBIDDEN,
                        Result.fail(403, "权限不足，无法访问该资源")))
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);
        // 允许同源 iframe（H2 控制台等场景，可选）
        http.headers().frameOptions().sameOrigin();
        return http.build();
    }

    /** 跨域配置：供前端 Vue3 开发服务器访问 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeJson(javax.servlet.http.HttpServletResponse response, HttpStatus status, Result<?> body)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
