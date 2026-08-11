package com.example.sample.service;

import com.octpus.core.annotation.ServiceName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 支付宝服务 - 演示一个类注册多个 OpenAPI。
 *
 * @author octpus-sample
 * @since 1.0.0
 */
@Slf4j
@Component
public class AlipayService {

    @ServiceName(interfaceName = "open.alipay.app.query", version = "1.0", description = "查询应用信息")
    public Map<String, Object> queryApp(Map<String, Object> request) {
        Objects.requireNonNull(request, "request cannot be null");
        String appId = (String) request.get("appId");
        log.info("[Alipay] queryApp: appId={}", appId);

        return Map.of(
                "appId", appId,
                "appName", "支付宝开放平台",
                "status", "ACTIVE"
        );
    }

    @ServiceName(interfaceName = "open.alipay.user.query", version = "1.0", description = "查询用户信息")
    public Map<String, Object> queryUser(Map<String, Object> request) {
        Objects.requireNonNull(request, "request cannot be null");
        String userId = (String) request.get("userId");
        log.info("[Alipay] queryUser: userId={}", userId);

        return Map.of(
                "userId", userId,
                "userName", "张三",
                "age", 28
        );
    }
}
