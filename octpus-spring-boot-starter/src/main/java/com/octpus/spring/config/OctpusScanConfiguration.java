package com.octpus.spring.config;

import com.octpus.core.annotation.ServiceName;
import com.octpus.core.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * 服务方法扫描 - 在所有单例 Bean 初始化完成后执行。
 *
 * @author octpus
 * @since 1.0.0
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

            while (targetClass.getName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            for (Method method : targetClass.getDeclaredMethods()) {
                ServiceName annotation = method.getAnnotation(ServiceName.class);
                if (annotation == null) continue;

                method.setAccessible(true);
                serviceRegistry.register(annotation.interfaceName(), bean, method);
                count++;
            }
        }

        log.info("[Octpus] {} service(s) registered", count);
    }
}
