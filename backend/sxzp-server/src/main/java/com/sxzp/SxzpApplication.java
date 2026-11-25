package com.sxzp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园实习招聘管理平台 - 后端启动类。
 *
 * <p>Mapper 扫描统一由 {@code com.sxzp.common.config.MybatisPlusConfig} 负责，
 * 此处不再重复声明 @MapperScan，避免重复注册告警。</p>
 */
@SpringBootApplication
public class SxzpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SxzpApplication.class, args);
    }
}
