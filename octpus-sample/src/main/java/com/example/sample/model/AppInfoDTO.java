package com.example.sample.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用信息响应 - 标准响应体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoDTO {
    private String appId;
    private String appName;
    private String status;
    private Integer category;
    private Long createTimestamp;
    private Double balance;
    private Boolean enabled;
}
