package com.octpus.core.annotation;

import java.lang.annotation.*;

/**
 * 服务名称注解 - 标记一个方法为八爪鱼网关可路由的接口。
 *
 * @author octpus
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceName {
    /** 接口名称，如 open.alipay.app.query */
    String interfaceName();
    /** 版本号 */
    String version() default "1.0";
    /** 接口描述 */
    String description() default "";
}
