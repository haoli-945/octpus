package com.octpus.core.model;

import lombok.Data;

/**
 * 网关统一请求体。
 *
 * @author haoli.xu
 * @since 1.0.0
 */
@Data
public class GatewayRequest {
    /** 接口名称 */
    private String serviceName;
    /** 版本号（可选，默认 1.0） */
    private String version = "1.0";
    /** 请求数据 */
    private Object data;
}
