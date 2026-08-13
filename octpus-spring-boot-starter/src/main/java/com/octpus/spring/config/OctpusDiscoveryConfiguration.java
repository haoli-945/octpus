package com.octpus.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octpus.core.discovery.ServiceDiscovery;
import com.octpus.core.invoker.RemoteInvoker;
import com.octpus.spring.discovery.DatabaseServiceDiscovery;
import com.octpus.spring.invoker.HttpRemoteInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 服务发现自动装配。
 * <p>
 * 条件装配逻辑：
 * <ul>
 *   <li>当应用配置了 DataSource 时，自动装配 DatabaseServiceDiscovery + HttpRemoteInvoker</li>
 *   <li>用户可自定义实现覆盖默认 Bean（@ConditionalOnMissingBean）</li>
 *   <li>未配置 DataSource 时，远程调用能力不激活（纯本地模式）</li>
 * </ul>
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Slf4j
@Configuration
@ConditionalOnBean(DataSource.class)
public class OctpusDiscoveryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceDiscovery serviceDiscovery(DataSource dataSource) {
        log.info("[Octpus] activating DatabaseServiceDiscovery (DataSource detected)");
        return new DatabaseServiceDiscovery(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteInvoker remoteInvoker(ObjectMapper objectMapper) {
        log.info("[Octpus] activating HttpRemoteInvoker (JDK HttpURLConnection)");
        return new HttpRemoteInvoker(objectMapper);
    }
}
