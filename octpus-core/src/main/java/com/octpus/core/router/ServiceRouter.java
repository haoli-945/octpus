package com.octpus.core.router;

import com.octpus.core.converter.ParamConverter;
import com.octpus.core.exception.OctpusException;
import com.octpus.core.model.ServiceMeta;
import com.octpus.core.registry.ServiceRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务路由器 - 核心组件，零外部依赖。
 *
 * @author octpus
 * @since 1.0.0
 */
public class ServiceRouter {

    private static final Logger log = Logger.getLogger(ServiceRouter.class.getName());
    private final ServiceRegistry serviceRegistry;
    private final ParamConverter paramConverter;

    public ServiceRouter(ServiceRegistry serviceRegistry, ParamConverter paramConverter) {
        this.serviceRegistry = serviceRegistry;
        this.paramConverter = paramConverter;
    }

    public Object route(String interfaceName, Object data) {
        ServiceMeta meta = serviceRegistry.lookup(interfaceName);
        if (meta == null) {
            throw new OctpusException(OctpusException.ERR_METHOD_NOT_FOUND,
                    "interface not found: " + interfaceName);
        }

        try {
            return invoke(meta, data);
        } catch (OctpusException e) {
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "[Octpus] invoke failed: " + interfaceName, e);
            throw new OctpusException(OctpusException.ERR_INVOKE_FAILED,
                    "invoke failed: " + e.getMessage(), e);
        }
    }

    private Object invoke(ServiceMeta meta, Object data) throws Exception {
        Method method = meta.getMethod();
        Parameter[] params = method.getParameters();

        if (params.length == 0) {
            return method.invoke(meta.getBean());
        }

        Class<?> paramType = params[0].getType();
        Object convertedData = paramConverter.convert(data, paramType);
        return method.invoke(meta.getBean(), convertedData);
    }
}
