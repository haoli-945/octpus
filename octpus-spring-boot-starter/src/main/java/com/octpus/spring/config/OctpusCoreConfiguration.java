package com.octpus.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octpus.core.converter.ParamConverter;
import com.octpus.core.discovery.ServiceDiscovery;
import com.octpus.core.invoker.RemoteInvoker;
import com.octpus.core.registry.ServiceRegistry;
import com.octpus.core.router.ServiceRouter;
import com.octpus.spring.converter.JacksonParamConverter;
import com.octpus.spring.gateway.GatewayController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心组件装配。
 * <p>
 * ServiceRouter 自动注入可选的 ServiceDiscovery 和 RemoteInvoker：
 * <ul>
 *   <li>两者都存在 → 完整的本地 + 远程路由能力</li>
 *   <li>仅本地模式 → 只有 ServiceRegistry + ParamConverter</li>
 * </ul>
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
    public ServiceRouter serviceRouter(
            ServiceRegistry serviceRegistry,
            ParamConverter paramConverter,
            @Autowired(required = false) ServiceDiscovery serviceDiscovery,
            @Autowired(required = false) RemoteInvoker remoteInvoker) {
        return new ServiceRouter(serviceRegistry, paramConverter, serviceDiscovery, remoteInvoker);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayController gatewayController(ServiceRouter serviceRouter) {
        return new GatewayController(serviceRouter);
    }
}
