package com.octpus.core.model;

import lombok.Data;

/**
 * 网关统一请求体。
 *
 * 支持两种方式：
 * 1. JSON: { "method": "xxx", "version": "1.0", "data": {...} }
 * 2. Multipart: form fields 中包含 method, version, data
 *
 * @author octpus
 * @since 1.0.0
 */
@Data
public class GatewayRequest {
    /** 接口名称 */
    private String method;
    /** 版本号（可选，默认 1.0） */
    private String version;
    /** 请求数据 */
    private Object data;
}
