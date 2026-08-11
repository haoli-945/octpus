package com.octpus.core.registry;

import com.octpus.core.model.ServiceMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 服务注册表 - 支持同接口多版本路由。
 *
 * @author octpus
 * @since 1.1.0
 */
public class ServiceRegistry {

    private static final Logger log = Logger.getLogger(ServiceRegistry.class.getName());
    private static final String DEFAULT_VERSION = "1.0";
    private final ConcurrentHashMap<String, ServiceMeta> registry = new ConcurrentHashMap<>();

    public void register(String interfaceName, String version, Object bean, Method method) {
        String key = buildKey(interfaceName, version);
        ServiceMeta meta = ServiceMeta.builder()
                .interfaceName(interfaceName)
                .version(version)
                .bean(bean)
                .method(method)
                .build();
        registry.put(key, meta);
        log.info("[Octpus] registered: " + interfaceName + " (v" + version + ") -> " +
                bean.getClass().getSimpleName() + "." + method.getName() + "()");
    }

    public ServiceMeta lookup(String interfaceName, String version) {
        if (version != null && !version.isBlank()) {
            ServiceMeta meta = registry.get(buildKey(interfaceName, version));
            if (meta != null) return meta;
        }
        return registry.get(buildKey(interfaceName, DEFAULT_VERSION));
    }

    public ServiceMeta lookup(String interfaceName) {
        return lookup(interfaceName, null);
    }

    public List<ServiceMeta> getVersions(String interfaceName) {
        List<ServiceMeta> versions = new ArrayList<>();
        for (ServiceMeta meta : registry.values()) {
            if (meta.getInterfaceName().equals(interfaceName)) {
                versions.add(meta);
            }
        }
        return versions;
    }

    public List<ServiceMeta> listAll() {
        return new ArrayList<>(registry.values());
    }

    public int size() {
        return registry.size();
    }

    private String buildKey(String interfaceName, String version) {
        String v = (version == null || version.isBlank()) ? DEFAULT_VERSION : version;
        return interfaceName + ":" + v;
    }
}
