package com.octpus.core.router;

import com.octpus.core.converter.ParamConverter;
import com.octpus.core.discovery.ServiceDiscovery;
import com.octpus.core.discovery.ServiceEndpoint;
import com.octpus.core.exception.OctpusErrorCode;
import com.octpus.core.exception.OctpusException;
import com.octpus.core.invoker.RemoteInvoker;
import com.octpus.core.model.ServiceMeta;
import com.octpus.core.registry.ServiceRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务路由器 - 支持版本路由、文件参数注入和远程服务代理。
 * <p>
 * 路由策略（优先级从高到低）：
 * <ol>
 *   <li>本地 ServiceRegistry 查找（同 JVM 内的 Bean 直接反射调用）</li>
 *   <li>远程 ServiceDiscovery 查找（通过 SPI 解析远程地址，HTTP 代理调用）</li>
 *   <li>均未找到则抛出 METHOD_NOT_FOUND 异常</li>
 * </ol>
 *
 * @author octpus
 * @since 1.4.0
 */
public class ServiceRouter {

    private static final Logger log = Logger.getLogger(ServiceRouter.class.getName());

    private final ServiceRegistry serviceRegistry;
    private final ParamConverter paramConverter;
    private final ServiceDiscovery serviceDiscovery;
    private final RemoteInvoker remoteInvoker;

    public ServiceRouter(ServiceRegistry serviceRegistry, ParamConverter paramConverter,
                         ServiceDiscovery serviceDiscovery, RemoteInvoker remoteInvoker) {
        this.serviceRegistry = serviceRegistry;
        this.paramConverter = paramConverter;
        this.serviceDiscovery = serviceDiscovery;
        this.remoteInvoker = remoteInvoker;
    }

    /**
     * 兼容旧构造函数（无远程能力时使用）。
     */
    public ServiceRouter(ServiceRegistry serviceRegistry, ParamConverter paramConverter) {
        this(serviceRegistry, paramConverter, null, null);
    }

    public Object route(String interfaceName, String version, Object data) {
        // 1. 本地优先：查 JVM 内注册表
        ServiceMeta meta = serviceRegistry.lookup(interfaceName, version);

        // 2. 远程兜底：通过 SPI 查找远程服务端点
        if (meta == null && serviceDiscovery != null) {
            ServiceEndpoint endpoint = serviceDiscovery.resolve(interfaceName, version);
            if (endpoint != null) {
                meta = ServiceMeta.builder()
                        .interfaceName(interfaceName)
                        .version(version)
                        .remoteUrl(endpoint.getUrl())
                        .timeoutMs(endpoint.getTimeoutMs())
                        .build();
                log.info("[Octpus] resolved remote: " + interfaceName + " -> " + endpoint.getUrl()
                        + " (system: " + endpoint.getSystemCode() + ")");
            }
        }

        // 3. 均未找到
        if (meta == null) {
            throw new OctpusException(
                    OctpusErrorCode.METHOD_NOT_FOUND,
                    "service not found: " + interfaceName
                            + (version != null ? " (version: " + version + ")" : "")
                            + " - not registered locally and no remote endpoint configured"
            );
        }

        // 4. 分发调用
        try {
            if (meta.getBean() != null) {
                return invokeLocal(meta, data);
            } else if (meta.getRemoteUrl() != null && remoteInvoker != null) {
                return remoteInvoker.invoke(
                        meta.getRemoteUrl(), interfaceName, version,
                        data, meta.getTimeoutMs()
                );
            } else {
                throw new OctpusException(
                        OctpusErrorCode.METHOD_NOT_FOUND,
                        "service found but no invoker available: " + interfaceName
                );
            }
        } catch (OctpusException e) {
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "[Octpus] invoke failed: " + interfaceName, e);
            throw new OctpusException(
                    OctpusErrorCode.INVOKE_FAILED,
                    "invoke failed: " + e.getMessage(), e
            );
        }
    }

    public Object route(String interfaceName, Object data) {
        return route(interfaceName, null, data);
    }

    // ==================== 本地调用 ====================

    @SuppressWarnings("unchecked")
    private Object invokeLocal(ServiceMeta meta, Object data) throws Exception {
        Method method = meta.getMethod();
        Parameter[] params = method.getParameters();

        if (params.length == 0) {
            return method.invoke(meta.getBean());
        }

        Class<?> paramType = params[0].getType();
        Object convertedData;

        // 检查是否包含文件参数
        if (data instanceof Map && ((Map<String, Object>) data).containsKey("_files")) {
            Map<String, Object> dataMap = (Map<String, Object>) data;

            // 提取文件（不参与 Jackson 转换）
            Object files = dataMap.remove("_files");

            // 转换剩余数据
            convertedData = paramConverter.convert(dataMap, paramType);

            // 通过反射注入文件到 Request 对象
            injectFiles(convertedData, files, paramType);
        } else {
            // 普通 JSON 请求
            convertedData = paramConverter.convert(data, paramType);
        }

        return method.invoke(meta.getBean(), convertedData);
    }

    /**
     * 通过反射注入文件到 Request 对象。
     */
    private void injectFiles(Object target, Object files, Class<?> targetType) {
        if (files == null) return;
        // 查找文件字段（类型为 MultipartFile 或 MultipartFile[]）
        for (java.lang.reflect.Field field : targetType.getDeclaredFields()) {
            if (field.getType().isArray() &&
                field.getType().getComponentType().getName().contains("MultipartFile")) {
                field.setAccessible(true);
                try {
                    field.set(target, files);
                } catch (IllegalAccessException e) {
                    throw new OctpusException(OctpusErrorCode.INVOKE_FAILED,
                            "failed to inject files: " + e.getMessage());
                }
                return;
            }
        }
    }
}
