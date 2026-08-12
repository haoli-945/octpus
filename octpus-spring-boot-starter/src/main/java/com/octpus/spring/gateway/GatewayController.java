package com.octpus.spring.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octpus.core.exception.OctpusErrorCode;
import com.octpus.core.exception.OctpusException;
import com.octpus.core.model.GatewayRequest;
import com.octpus.core.model.GatewayResponse;
import com.octpus.core.router.ServiceRouter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;

/**
 * 网关统一入口 - 自动识别 JSON/Multipart。
 *
 * @author octpus
 * @since 1.3.0
 */
@Slf4j
@RestController
public class GatewayController {

    private static final String TRACE_ID_KEY = "TraceId";
    private final ServiceRouter serviceRouter;
    private final ObjectMapper objectMapper;

    public GatewayController(ServiceRouter serviceRouter) {
        this.serviceRouter = serviceRouter;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/service.do")
    public GatewayResponse<?> handle(HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID_KEY, traceId);

        try {
            GatewayRequest gatewayRequest;
            String contentType = request.getContentType();

            if (contentType != null && contentType.contains("multipart/")) {
                gatewayRequest = parseMultipartRequest((MultipartHttpServletRequest) request);
                log.info("[Octpus] received (multipart): method={}, traceId={}",
                        gatewayRequest.getServiceName(), traceId);
            } else {
                gatewayRequest = parseJsonRequest(request);
                log.info("[Octpus] received (json): method={}, version={}, traceId={}",
                        gatewayRequest.getServiceName(), gatewayRequest.getVersion(), traceId);
            }

            if (gatewayRequest.getServiceName() == null || gatewayRequest.getServiceName().isBlank()) {
                throw new OctpusException(OctpusErrorCode.METHOD_MISSING);
            }

            Object result = serviceRouter.route(
                    gatewayRequest.getServiceName(),
                    gatewayRequest.getVersion(),
                    gatewayRequest.getData()
            );

            if (result instanceof GatewayResponse) {
                return (GatewayResponse<?>) result;
            }
            return GatewayResponse.success(result);

        } catch (OctpusException e) {
            log.warn("[Octpus] error: code={}, desc={}, message={}, traceId={}",
                    e.getCode(), e.getDesc(), e.getMessage(), traceId);
            throw e;
        } catch (Exception e) {
            log.error("[Octpus] system error: traceId={}", traceId, e);
            throw new OctpusException(OctpusErrorCode.INVOKE_FAILED, "system error: " + e.getMessage(), e);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private GatewayRequest parseJsonRequest(HttpServletRequest request) throws Exception {
        return objectMapper.readValue(request.getInputStream(), GatewayRequest.class);
    }

    @SuppressWarnings("unchecked")
    private GatewayRequest parseMultipartRequest(MultipartHttpServletRequest request) {
        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setServiceName(request.getParameter("serviceName"));
        gatewayRequest.setVersion(request.getParameter("version"));

        // 提取所有文件（支持同名字段多文件）
        List<MultipartFile> allFiles = new ArrayList<>();
        Set<String> processedNames = new HashSet<>();

        for (String name : request.getFileMap().keySet()) {
            if (!processedNames.contains(name)) {
                allFiles.addAll(request.getFiles(name));
                processedNames.add(name);
            }
        }
        MultipartFile[] files = allFiles.toArray(new MultipartFile[0]);

        // 提取 data 字段
        String dataStr = request.getParameter("data");
        if (dataStr != null && !dataStr.isBlank()) {
            try {
                Object dataObj = objectMapper.readValue(dataStr, Object.class);

                if (files.length > 0) {
                    if (dataObj instanceof Map) {
                        Map<String, Object> dataMap = new HashMap<>((Map<String, Object>) dataObj);
                        dataMap.put("_files", files);
                        gatewayRequest.setData(dataMap);
                    } else {
                        Map<String, Object> wrapper = new HashMap<>();
                        wrapper.put("_data", dataObj);
                        wrapper.put("_files", files);
                        gatewayRequest.setData(wrapper);
                    }
                } else {
                    gatewayRequest.setData(dataObj);
                }
            } catch (Exception e) {
                throw new OctpusException(OctpusErrorCode.PARAM_PARSE, "data解析失败: " + e.getMessage());
            }
        } else if (files.length > 0) {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("_files", files);
            gatewayRequest.setData(wrapper);
        }

        return gatewayRequest;
    }

    @ExceptionHandler(OctpusException.class)
    @ResponseStatus
    public GatewayResponse<?> handleException(OctpusException e) {
        return GatewayResponse.fail(e.getCode(), e.getDesc());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public GatewayResponse<?> handleException(Exception e) {
        return GatewayResponse.error("system error: " + e.getMessage());
    }
}
