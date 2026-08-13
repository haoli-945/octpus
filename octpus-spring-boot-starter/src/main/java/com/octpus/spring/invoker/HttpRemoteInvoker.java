package com.octpus.spring.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octpus.core.exception.OctpusErrorCode;
import com.octpus.core.exception.OctpusException;
import com.octpus.core.invoker.RemoteInvoker;
import com.octpus.core.model.GatewayRequest;
import com.octpus.core.model.GatewayResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 基于 JDK HttpURLConnection 的远程调用实现。
 * <p>
 * 零外部依赖（仅依赖 JDK + Jackson），适用于大多数场景。
 * 调用链路：
 * <pre>
 *   本地 GatewayRequest → Jackson 序列化 → HTTP POST → 远程 /service.do
 *   → 读取响应 → Jackson 反序列化为 GatewayResponse → 返回
 * </pre>
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Slf4j
public class HttpRemoteInvoker implements RemoteInvoker {

    private final ObjectMapper objectMapper;

    public HttpRemoteInvoker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(String url, String serviceName, String version,
                         Object data, int timeoutMs) throws OctpusException {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 1. 构建统一请求体
        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setServiceName(serviceName);
        gatewayRequest.setVersion(version);
        gatewayRequest.setData(data);

        // 2. 序列化
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(gatewayRequest);
        } catch (Exception e) {
            throw new OctpusException(OctpusErrorCode.PARAM_PARSE,
                    "failed to serialize remote request: " + e.getMessage());
        }

        // 3. 发送 HTTP POST
        log.info("[Octpus] remote invoke: {} -> {} (traceId={})", serviceName, url, traceId);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-Octpus-Trace-Id", traceId);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 4. 读取响应
            int httpCode = conn.getResponseCode();
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (httpCode != 200) {
                log.warn("[Octpus] remote HTTP error: {} -> {} (code={})", serviceName, url, httpCode);
                throw new OctpusException(OctpusErrorCode.INVOKE_FAILED,
                        "remote service returned HTTP " + httpCode + ": " + responseBody);
            }

            // 5. 反序列化
            GatewayResponse<?> gatewayResponse = objectMapper.readValue(responseBody, GatewayResponse.class);

            log.info("[Octpus] remote invoke success: {} (code={}, traceId={})",
                    serviceName, gatewayResponse.getCode(), traceId);

            return gatewayResponse;

        } catch (OctpusException e) {
            throw e;
        } catch (IOException e) {
            log.error("[Octpus] remote invoke IO error: {} -> {}", serviceName, url, e);
            throw new OctpusException(OctpusErrorCode.INVOKE_FAILED,
                    "remote invoke failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
