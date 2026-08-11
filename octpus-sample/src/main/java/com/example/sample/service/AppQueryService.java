package com.example.sample.service;

import com.octpus.core.annotation.ServiceName;

import java.util.Map;

/**
 * 应用查询接口 - @ServiceName 在接口方法上定义契约。
 *
 * 路由规则：
 * - interfaceName 由接口方法定义
 * - version 由实现类的 @Version 决定
 * - 前端传 version 参数选择具体实现
 */
public interface AppQueryService {

    @ServiceName(interfaceName = "open.alipay.app.query", description = "查询应用信息")
    Map<String, Object> query(Map<String, Object> request);
}
