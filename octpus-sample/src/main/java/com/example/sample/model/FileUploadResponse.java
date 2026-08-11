package com.example.sample.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件上传响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private List<String> fileIds;
    private Long totalSize;
    private Integer fileCount;
}
