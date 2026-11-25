package com.sxzp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具：签发与解析令牌。
 */
@Slf4j
@Component
public class JwtUtils {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌。
     *
     * @param userId   用户ID
     * @param account  登录账号
     * @param userType 用户类型 student/company/admin
     */
    public String createToken(Long userId, String account, String userType) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + properties.getExpire());
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("userId", userId);
        claims.put("account", account);
        claims.put("userType", userType);
        return Jwts.builder()
                .claims(claims)
                .subject(account)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析令牌，失败返回 null。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求头值中剥离前缀。
     */
    public String resolveToken(String headerValue) {
        if (headerValue == null || headerValue.isEmpty()) {
            return null;
        }
        String prefix = properties.getPrefix();
        if (prefix != null && !prefix.isEmpty() && headerValue.startsWith(prefix)) {
            return headerValue.substring(prefix.length()).trim();
        }
        return headerValue.trim();
    }

    public String getHeader() {
        return properties.getHeader();
    }
}
