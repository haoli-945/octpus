package com.octpus.core.router;

import com.octpus.core.converter.ParamConverter;
import com.octpus.core.exception.OctpusErrorCode;
import com.octpus.core.exception.OctpusException;
import com.octpus.core.model.ServiceMeta;
import com.octpus.core.registry.ServiceRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务路由器 - 支持版本路由和文件参数注入。
 *
 * @author octpus
 * @since 1.3.0
 */
public class ServiceRouter {

    private static final Logger log = Logger.getLogger(ServiceRouter.class.getName());
    private final ServiceRegistry serviceRegistry;
    private final ParamConverter paramConverter;

    public ServiceRouter(ServiceRegistry serviceRegistry, ParamConverter paramConverter) {
        this.serviceRegistry = serviceRegistry;
        this.paramConverter = paramConverter;
    }

    public Object route(String interfaceName, String version, Object data) {
        ServiceMeta meta = serviceRegistry.lookup(interfaceName, version);
        if (meta == null) {
            throw new OctpusException(
                    OctpusErrorCode.METHOD_NOT_FOUND,
                    "interface not found: " + interfaceName + " (version: " + version + ")"
            );
        }

        try {
            return invoke(meta, data);
        } catch (OctpusException e) {
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "[Octpus] invoke failed: " + interfaceName, e);
            throw new OctpusException(
                    OctpusErrorCode.INVOKE_FAILED,
                    "invoke failed: " + e.getMessage(),
                    e
            );
        }
    }

    public Object route(String interfaceName, Object data) {
        return route(interfaceName, null, data);
    }

    @SuppressWarnings("unchecked")
    private Object invoke(ServiceMeta meta, Object data) throws Exception {
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
