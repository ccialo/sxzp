package com.sxzp.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 相关配置，对应 application.yml 中 sxzp.jwt.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "sxzp.jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secret = "sxzp-default-secret-key-please-change-in-production";

    /** 有效期(毫秒) */
    private long expire = 86400000L;

    /** 携带令牌的请求头 */
    private String header = "Authorization";

    /** 令牌前缀 */
    private String prefix = "Bearer ";
}
