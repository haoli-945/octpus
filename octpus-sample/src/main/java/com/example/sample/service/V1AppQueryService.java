package com.example.sample.service;

import com.octpus.core.annotation.ServiceName;
import com.octpus.core.annotation.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * V1 版本 - 默认版本。
 */
@Slf4j
@Component
@Version("1.0")
public class V1AppQueryService implements AppQueryService {

    @Override
    @ServiceName(interfaceName = "open.alipay.app.query", description = "查询应用信息 V1")
    public Map<String, Object> query(Map<String, Object> request) {
        Objects.requireNonNull(request, "request cannot be null");
        String appId = (String) request.get("appId");
        log.info("[AppQuery-V1] query: appId={}", appId);

        return Map.of(
                "version", "1.0",
                "appId", appId,
                "appName", "支付宝开放平台",
                "status", "ACTIVE"
        );
    }
}
