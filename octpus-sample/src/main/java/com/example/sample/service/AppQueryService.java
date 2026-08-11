package com.example.sample.service;

import java.util.Map;

/**
 * 应用查询接口 - 定义服务契约。
 */
public interface AppQueryService {
    Map<String, Object> query(Map<String, Object> request);
}
