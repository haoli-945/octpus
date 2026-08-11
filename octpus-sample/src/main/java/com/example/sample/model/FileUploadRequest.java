package com.example.sample.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求 - 框架自动注入文件。
 *
 * 前端调用：
 * POST /service.do
 * Content-Type: multipart/form-data
 * form fields:
 *   - method: "open.alipay.file.upload"
 *   - version: "1.0"
 *   - data: {"bizType":"contract","description":"合同文件"}
 *   - files: [file1, file2, ...]
 */
@Data
public class FileUploadRequest {
    /** 业务类型 */
    private String bizType;
    /** 文件描述 */
    private String description;
    /** 文件数组 - 框架自动注入 */
    private MultipartFile[] files;
}
