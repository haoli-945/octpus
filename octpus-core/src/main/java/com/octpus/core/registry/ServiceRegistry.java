package com.octpus.core.registry;

import com.octpus.core.model.ServiceMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 服务注册表 - 核心组件，零外部依赖。
 *
 * @author octpus
 * @since 1.0.0
 */
public class ServiceRegistry {

    private static final Logger log = Logger.getLogger(ServiceRegistry.class.getName());
    private final ConcurrentHashMap<String, ServiceMeta> registry = new ConcurrentHashMap<>();

    public void register(String interfaceName, Object bean, Method method) {
        ServiceMeta meta = ServiceMeta.builder()
                .interfaceName(interfaceName)
                .bean(bean)
                .method(method)
                .build();
        registry.put(interfaceName, meta);
        log.info("[Octpus] registered: " + interfaceName + " -> " +
                bean.getClass().getSimpleName() + "." + method.getName() + "()");
    }

    public ServiceMeta lookup(String interfaceName) {
        return registry.get(interfaceName);
    }

    public List<ServiceMeta> listAll() {
        return new ArrayList<>(registry.values());
    }

    public int size() {
        return registry.size();
    }
}
