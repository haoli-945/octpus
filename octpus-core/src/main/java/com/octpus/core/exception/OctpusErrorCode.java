package com.octpus.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 八爪鱼异常错误码枚举。
 * <p>code 对外返回，desc 用于自文档化说明。</p>
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Getter
@AllArgsConstructor
public enum OctpusErrorCode {

    /** 请求的 serviceName 不能为空 */
    METHOD_MISSING("100001", "serviceName不能为空"),

    /** 接口未注册或版本不存在 */
    METHOD_NOT_FOUND("100002", "接口未注册或版本不存在"),

    /** 请求参数解析失败 */
    PARAM_PARSE("100003", "请求参数解析失败"),

    /** 服务调用失败 */
    INVOKE_FAILED("100004", "服务调用失败"),

    /** 内部系统异常 */
    INTERNAL_SYSTEM_ERROR("100005","内部系统异常"),

    ;

    /** 对外返回的错误码 */
    private final String code;

    /** 错误描述（自文档化，无需翻代码即可理解） */
    private final String desc;
}
