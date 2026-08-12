package com.example.sample.service;

import com.octpus.core.annotation.Version;
import com.octpus.core.model.GatewayResponse;
import com.example.sample.model.FileUploadRequest;
import com.example.sample.model.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件上传实现。
 */
@Slf4j
@Component
@Version("1.0")
public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public GatewayResponse<FileUploadResponse> upload(FileUploadRequest request) {
        log.info("[FileUpload] bizType={}, description={}, fileCount={}",
                request.getBizType(),
                request.getDescription(),
                request.getFiles() != null ? request.getFiles().length : 0);

        List<String> fileIds = new ArrayList<>();
        long totalSize = 0;

        if (request.getFiles() != null) {
            for (var file : request.getFiles()) {
                String fileId = UUID.randomUUID().toString();
                fileIds.add(fileId);
                totalSize += file.getSize();
                log.info("[FileUpload] uploaded: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            }
        }

        FileUploadResponse response = new FileUploadResponse(
                fileIds,
                totalSize,
                fileIds.size()
        );

        return GatewayResponse.success(response);
    }
}
