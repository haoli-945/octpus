package com.octpus.spring.config;

import com.octpus.core.annotation.ServiceName;
import com.octpus.core.annotation.Version;
import com.octpus.core.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * 服务方法扫描 - 支持 @ServiceName + @Version。
 *
 * 扫描策略：
 * 1. 扫描所有 Bean 中带 @ServiceName 的方法
 * 2. 检查 Bean 类上是否有 @Version 注解
 * 3. 如果有 @Version，使用指定版本号
 * 4. 如果没有 @Version，使用默认版本 "1.0"
 *
 * @author octpus
 * @since 1.1.0
 */
@Slf4j
@Configuration
public class OctpusScanConfiguration implements InitializingBean {

    private final ApplicationContext applicationContext;
    private final ServiceRegistry serviceRegistry;

    public OctpusScanConfiguration(ApplicationContext applicationContext, ServiceRegistry serviceRegistry) {
        this.applicationContext = applicationContext;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int count = 0;

        for (String beanName : beanNames) {
            if (beanName.contains("octpus") || beanName.contains("Octpus")) {
                continue;
            }

            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = bean.getClass();

            // 跳过 CGLIB 代理
            while (targetClass.getName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            // 获取 @Version 注解（如果有）
            String version = "1.0";
            Version versionAnnotation = targetClass.getAnnotation(Version.class);
            if (versionAnnotation != null) {
                version = versionAnnotation.value();
            }

            // 扫描 @ServiceName 方法
            for (Method method : targetClass.getDeclaredMethods()) {
                ServiceName annotation = method.getAnnotation(ServiceName.class);
                if (annotation == null) continue;

                method.setAccessible(true);

                // 使用注解中的 version（如果指定），否则使用类上的 @Version
                String finalVersion = annotation.version().equals("1.0") ? version : annotation.version();

                serviceRegistry.register(
                        annotation.interfaceName(),
                        finalVersion,
                        bean,
                        method
                );
                count++;
            }
        }

        log.info("[Octpus] {} service(s) registered", count);
    }
}
