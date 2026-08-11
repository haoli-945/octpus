package com.octpus.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octpus.core.converter.ParamConverter;
import com.octpus.core.registry.ServiceRegistry;
import com.octpus.core.router.ServiceRouter;
import com.octpus.spring.converter.JacksonParamConverter;
import com.octpus.spring.gateway.GatewayController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心组件装配。
 *
 * @author haoli.xu
 * @since 1.0.0
 */
@Configuration
public class OctpusCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ParamConverter paramConverter(ObjectMapper objectMapper) {
        return new JacksonParamConverter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry() {
        return new ServiceRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceRouter serviceRouter(ServiceRegistry serviceRegistry, ParamConverter paramConverter) {
        return new ServiceRouter(serviceRegistry, paramConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayController gatewayController(ServiceRouter serviceRouter) {
        return new GatewayController(serviceRouter);
    }
}
