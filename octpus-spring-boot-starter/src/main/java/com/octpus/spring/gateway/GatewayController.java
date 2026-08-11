package com.octpus.spring.gateway;

import com.octpus.core.exception.OctpusException;
import com.octpus.core.model.GatewayRequest;
import com.octpus.core.model.GatewayResponse;
import com.octpus.core.router.ServiceRouter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 网关统一入口 - 支持版本路由。
 *
 * @author haoli.xu
 * @since 1.1.0
 */
@Slf4j
@RestController
public class GatewayController {

    private static final String TRACE_ID_KEY = "TraceId";
    private final ServiceRouter serviceRouter;

    public GatewayController(ServiceRouter serviceRouter) {
        this.serviceRouter = serviceRouter;
    }

    @PostMapping("/service.do")
    public GatewayResponse<?> handle(@RequestBody GatewayRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID_KEY, traceId);

        try {
            String method = request.getMethod();
            String version = request.getVersion();

            log.info("[Octpus] received: method={}, version={}, traceId={}", method, version, traceId);

            if (method == null || method.isBlank()) {
                throw new OctpusException(OctpusException.ERR_METHOD_MISSING, "method cannot be empty");
            }

            Object result = serviceRouter.route(method, version, request.getData());
            log.info("[Octpus] completed: method={}, version={}, traceId={}", method, version, traceId);
            return GatewayResponse.success(result);

        } catch (OctpusException e) {
            log.warn("[Octpus] error: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[Octpus] system error: traceId={}", traceId, e);
            throw new OctpusException(OctpusException.ERR_INVOKE_FAILED, "system error: " + e.getMessage(), e);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    @ExceptionHandler(OctpusException.class)
    @ResponseStatus
    public GatewayResponse<?> handleException(OctpusException e) {
        return GatewayResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public GatewayResponse<?> handleException(Exception e) {
        return GatewayResponse.error("system error: " + e.getMessage());
    }
}
