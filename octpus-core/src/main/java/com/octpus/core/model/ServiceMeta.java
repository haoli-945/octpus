package com.octpus.core.model;

import lombok.Builder;
import lombok.Data;
import java.lang.reflect.Method;

/**
 * 服务元信息 - 封装一个已注册服务方法的所有必要信息。
 *
 * @author octpus
 * @since 1.0.0
 */
@Data
@Builder
public class ServiceMeta {
    private String interfaceName;
    private String version;
    private String description;
    private Object bean;
    private Method method;
}
