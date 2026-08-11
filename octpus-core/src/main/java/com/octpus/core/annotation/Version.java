package com.octpus.core.annotation;

import java.lang.annotation.*;

/**
 * 版本注解 - 标记实现类的版本号，用于同接口多版本路由。
 *
 * 使用示例：
 * <pre>
 * {@literal @}Component
 * {@literal @}Version("1.0")
 * public class V1AppQueryService implements AppQueryService { ... }
 *
 * {@literal @}Component
 * {@literal @}Version("2.0")
 * public class V2AppQueryService implements AppQueryService { ... }
 * </pre>
 *
 * 路由规则：
 * - 前端传 version="1.0" → 路由到 V1AppQueryService
 * - 前端传 version="2.0" → 路由到 V2AppQueryService
 * - 前端不传 version → 路由到默认版本（1.0）
 *
 * @author octpus
 * @since 1.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Version {
    /**
     * 版本号，默认 1.0。
     */
    String value() default "1.0";
}
