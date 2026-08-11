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
 * 服务方法扫描 - 支持接口级 @ServiceName + 实现类 @Version。
 *
 * 扫描策略：
 * 1. 扫描 Bean 实现的接口，查找带 @ServiceName 的方法
 * 2. 检查实现类上是否有 @Version 注解
 * 3. 注册：interfaceName（来自接口）+ version（来自实现类）
 *
 * @author octpus
 * @since 1.2.0
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

            // 获取 @Version
            String version = "1.0";
            Version versionAnnotation = targetClass.getAnnotation(Version.class);
            if (versionAnnotation != null) {
                version = versionAnnotation.value();
            }

            // 扫描接口中的 @ServiceName 方法
            count += scanInterfaces(targetClass, bean, version);

            // 兼容：扫描类自身的 @ServiceName 方法
            count += scanDeclaredMethods(targetClass, bean, version);
        }

        log.info("[Octpus] {} service(s) registered", count);
    }

    /**
     * 扫描接口中的 @ServiceName 方法。
     */
    private int scanInterfaces(Class<?> targetClass, Object bean, String version) {
        int count = 0;
        for (Class<?> iface : targetClass.getInterfaces()) {
            // 跳过 Spring 和 JDK 内置接口
            if (iface.getName().startsWith("org.springframework.") ||
                iface.getName().startsWith("java.")) {
                continue;
            }

            for (Method method : iface.getDeclaredMethods()) {
                ServiceName annotation = method.getAnnotation(ServiceName.class);
                if (annotation == null) continue;

                method.setAccessible(true);
                serviceRegistry.register(annotation.interfaceName(), version, bean, method);
                count++;
            }
        }
        return count;
    }

    /**
     * 扫描类自身的 @ServiceName 方法（兼容旧用法）。
     */
    private int scanDeclaredMethods(Class<?> targetClass, Object bean, String version) {
        int count = 0;
        for (Method method : targetClass.getDeclaredMethods()) {
            ServiceName annotation = method.getAnnotation(ServiceName.class);
            if (annotation == null) continue;

            method.setAccessible(true);
            String finalVersion = annotation.version().equals("1.0") ? version : annotation.version();
            serviceRegistry.register(annotation.interfaceName(), finalVersion, bean, method);
            count++;
        }
        return count;
    }
}
