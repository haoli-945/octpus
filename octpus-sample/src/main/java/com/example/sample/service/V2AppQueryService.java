package com.example.sample.service;

import com.octpus.core.annotation.Version;
import com.octpus.core.model.GatewayResponse;
import com.example.sample.model.AppInfoDTO;
import com.example.sample.model.AppQueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Version("2.0")
public class V2AppQueryService implements AppQueryService {

    @Override
    public GatewayResponse<AppInfoDTO> query(AppQueryRequest request) {
        log.info("[AppQuery-V2] query: appId={}, appName={}",
                request.getAppId(), request.getAppName());

        AppInfoDTO dto = new AppInfoDTO(
                request.getAppId(),
                request.getAppName(),
                "ACTIVE",
                request.getCategory(),
                request.getCreateTimestamp(),
                request.getBalance(),
                request.getEnabled()
        );

        return GatewayResponse.success(dto);
    }
}
