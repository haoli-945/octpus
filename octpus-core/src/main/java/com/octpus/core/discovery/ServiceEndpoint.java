package com.octpus.core.discovery;

import lombok.Builder;
import lombok.Data;

/**
 * 服务端点 - 描述一个远程服务的访问地址和元信息。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Data
@Builder
public class ServiceEndpoint {

    /** 服务完整访问地址，如 http://192.168.1.100:8080/service.do */
    private String url;

    /** 负载均衡权重，默认 1 */
    @Builder.Default
    private int weight = 1;

    /** 调用超时时间（毫秒），默认 3000 */
    @Builder.Default
    private int timeoutMs = 3000;

    /** 服务提供方系统编码 */
    private String systemCode;

    /** 服务提供方系统名称 */
    private String systemName;
}
