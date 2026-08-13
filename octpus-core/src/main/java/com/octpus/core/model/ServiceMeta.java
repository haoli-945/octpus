package com.octpus.core.model;

import lombok.Builder;
import lombok.Data;
import java.lang.reflect.Method;

/**
 * 服务元信息 - 封装一个已注册服务方法的所有必要信息。
 * <p>
 * 支持两种模式：
 * <ul>
 *   <li>本地模式：bean + method 不为空，直接反射调用</li>
 *   <li>远程模式：remoteUrl 不为空，通过 RemoteInvoker HTTP 代理</li>
 * </ul>
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

    // ===== 本地模式字段 =====
    /** 目标 Bean（远程模式时为 null） */
    private Object bean;
    /** 目标方法（远程模式时为 null） */
    private Method method;

    // ===== 远程模式字段 =====
    /** 远程服务完整地址（本地模式时为 null），如 http://192.168.1.100:8080/service.do */
    private String remoteUrl;
    /** 远程调用超时时间（毫秒），默认 3000 */
    @Builder.Default
    private int timeoutMs = 3000;
}
