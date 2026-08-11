package com.example.sample.service;

import com.octpus.core.annotation.ServiceName;
import com.octpus.core.model.GatewayResponse;
import com.example.sample.model.AppInfoDTO;
import com.example.sample.model.AppQueryRequest;

/**
 * 应用查询接口 - 标准 Request/Response 设计。
 */
public interface AppQueryService {

    @ServiceName(interfaceName = "open.alipay.app.query", description = "查询应用信息")
    GatewayResponse<AppInfoDTO> query(AppQueryRequest request);
}
