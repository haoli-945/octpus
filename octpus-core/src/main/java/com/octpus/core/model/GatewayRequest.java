package com.octpus.core.model;

import lombok.Data;

/**
 * 网关统一请求体。
 *
 * @author octpus
 * @since 1.0.0
 */
@Data
public class GatewayRequest {
    private String method;
    private Object data;
}
