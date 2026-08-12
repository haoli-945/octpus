package com.example.sample.service;

import com.octpus.core.annotation.ServiceName;
import com.octpus.core.model.GatewayResponse;
import com.example.sample.model.FileUploadResponse;
import com.example.sample.model.FileUploadRequest;

/**
 * 文件上传接口 - 定义契约。
 */
public interface FileUploadService {

    @ServiceName(interfaceName = "open.alipay.file.upload", description = "上传文件")
    GatewayResponse<FileUploadResponse> upload(FileUploadRequest request);
}
