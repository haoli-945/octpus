package com.octpus.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关统一响应体。
 *
 * @author haoli.xu
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayResponse<T> {
    private String code;
    private String message;
    private T data;

    public static <T> GatewayResponse<T> success(T data) {
        return new GatewayResponse<>("000000", "SUCCESS", data);
    }

    public static <T> GatewayResponse<T> fail(String code, String message) {
        return new GatewayResponse<>(code, message, null);
    }

    public static <T> GatewayResponse<T> error(String message) {
        return new GatewayResponse<>("999999", message, null);
    }
}
