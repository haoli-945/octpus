package com.octpus.core.discovery;

/**
 * 服务发现 SPI - 定义服务路由的抽象契约。
 * <p>
 * 框架核心层只定义接口，具体实现由适配层提供：
 * <ul>
 *   <li>DatabaseServiceDiscovery - 基于数据库的实现（推荐）</li>
 *   <li>NacosServiceDiscovery - 基于 Nacos 的实现（可扩展）</li>
 *   <li>ConsulServiceDiscovery - 基于 Consul 的实现（可扩展）</li>
 * </ul>
 *
 * @author haoli.xu
 * @since 1.4.0
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名解析目标服务端点。
     *
     * @param serviceName 服务名称（全局唯一）
     * @param version     版本号（可选）
     * @return 服务端点信息，未找到返回 null
     */
    ServiceEndpoint resolve(String serviceName, String version);

    /**
     * 根据服务名解析目标服务端点（使用默认版本）。
     */
    default ServiceEndpoint resolve(String serviceName) {
        return resolve(serviceName, null);
    }
}
