package com.example.sample.service;

import com.octpus.core.annotation.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * V2 版本 - 新增更多字段。
 * 注意：不需要 @ServiceName，接口已定义。
 */
@Slf4j
@Component
@Version("2.0")
public class V2AppQueryService implements AppQueryService {

    @Override
    public Map<String, Object> query(Map<String, Object> request) {
        Objects.requireNonNull(request, "request cannot be null");
        String appId = (String) request.get("appId");
        log.info("[AppQuery-V2] query: appId={}", appId);

        return Map.of(
                "version", "2.0",
                "appId", appId,
                "appName", "支付宝开放平台",
                "status", "ACTIVE",
                "level", "PREMIUM",
                "quota", 1000000
        );
    }
}
