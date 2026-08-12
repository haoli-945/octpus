package com.example.sample.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应用查询请求参数 - 接入方自定义。
 *
 * 前端调用：
 * {
 *   "serviceName": "open.alipay.app.query",
 *   "version": "1.0",
 *   "data": { "appId": "123", "appName": "小程序123" }
 * }
 *
 * data 中的键值对会自动映射到此对象。
 */
@Data
public class AppQueryRequest {
    private String appId;
    private String appName;
    private Integer category;
    private Long createTimestamp;
    private Double balance;
    private Boolean enabled;
    private MultipartFile[] multipartFiles;
}
