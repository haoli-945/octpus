package com.octpus.core.invoker;

import com.octpus.core.exception.OctpusException;

/**
 * 远程调用 SPI - 定义向远程服务发起 HTTP 请求的抽象契约。
 * <p>
 * 核心层只定义接口，具体实现（HttpURLConnection / OkHttp / WebClient 等）
 * 由适配层提供，确保 octpus-core 零外部依赖。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
public interface RemoteInvoker {

    /**
     * 调用远程服务。
     *
     * @param url         远程服务完整地址
     * @param serviceName 服务名称（写入请求体）
     * @param version     版本号（写入请求体，可选）
     * @param data        请求数据（业务参数）
     * @param timeoutMs   超时时间（毫秒）
     * @return 远程服务返回的结果（已反序列化）
     * @throws OctpusException 调用失败时抛出
     */
    Object invoke(String url, String serviceName, String version,
                  Object data, int timeoutMs) throws OctpusException;
}
