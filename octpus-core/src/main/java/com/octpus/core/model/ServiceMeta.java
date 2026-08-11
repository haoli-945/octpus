package com.octpus.core.model;

import lombok.Builder;
import lombok.Data;
import java.lang.reflect.Method;

/**
 * 服务元信息 - 封装一个已注册服务方法的所有必要信息。
 *
 * @author haoli.xu
 * @since 1.0.0
 */
@Data
@Builder
public class ServiceMeta {
    /** 接口名称 */
    private String interfaceName;
    /** 版本号 */
    private String version;
    /** 接口描述 */
    private String description;
    /** 目标 Bean */
    private Object bean;
    /** 目标方法 */
    private Method method;
}
