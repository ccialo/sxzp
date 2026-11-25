package com.sxzp.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置：上传文件静态资源映射等。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${sxzp.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * 将 /uploads/** 映射到本地上传目录，使上传的文件可通过 HTTP 访问。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = new File(uploadDir).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
