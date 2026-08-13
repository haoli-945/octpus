package com.octpus.spring.admin;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Admin 模块自动装配 - 条件：存在 DataSource。
 * <p>
 * 配置 CORS 允许前端跨域访问管理 API。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Configuration
@ConditionalOnBean(DataSource.class)
public class OctpusAdminConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/octpus/admin/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
