package com.sxzp.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 接口文档生成器：启动时扫描所有 Controller 的 RequestMapping，
 * 生成接口清单供 /api/doc 端点展示。
 */
@Slf4j
@Component
public class ApiDocScanner {

    @Autowired
    private ApplicationContext applicationContext;

    private List<ApiDocEntry> entries = new ArrayList<>();

    @PostConstruct
    public void scan() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();

            Set<String> patterns;
            if (info.getPatternsCondition() != null) {
                patterns = info.getPatternsCondition().getPatterns();
            } else if (info.getPathPatternsCondition() != null) {
                patterns = info.getPathPatternsCondition().getPatternValues();
            } else {
                continue;
            }

            for (String pattern : patterns) {
                ApiDocEntry doc = new ApiDocEntry();
                doc.setPath(pattern);
                doc.setMethods(info.getMethodsCondition().toString()
                        .replace("[", "").replace("]", "").replace("HEAD", "").trim());
                if (doc.getMethods().isEmpty()) {
                    doc.setMethods("ALL");
                }
                // 提取模块名（从包路径）
                String pkg = method.getBeanType().getPackage().getName();
                String module = pkg.substring(pkg.lastIndexOf('.') + 1);
                doc.setModule(module);
                doc.setHandler(method.getBeanType().getSimpleName() + "." + method.getMethod().getName());
                // 尝试从方法上获取 PreAuthorize 注解
                javax.annotation.security.RolesAllowed rolesAllowed =
                        method.getMethodAnnotation(javax.annotation.security.RolesAllowed.class);
                if (rolesAllowed != null) {
                    doc.setRoles(String.join(", ", rolesAllowed.value()));
                }
                org.springframework.security.access.prepost.PreAuthorize preAuth =
                        method.getMethodAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
                if (preAuth != null) {
                    doc.setRoles(preAuth.value());
                }
                entries.add(doc);
            }
        }

        entries.sort(Comparator.comparing(ApiDocEntry::getPath));
        log.info("API 文档扫描完成: 共 {} 个接口", entries.size());
    }

    public List<ApiDocEntry> getEntries() {
        return entries;
    }

    public List<String> getModules() {
        return entries.stream().map(ApiDocEntry::getModule)
                .distinct().sorted().collect(Collectors.toList());
    }

    @Data
    public static class ApiDocEntry {
        private String path;
        private String methods;
        private String module;
        private String handler;
        private String roles;
    }
}
