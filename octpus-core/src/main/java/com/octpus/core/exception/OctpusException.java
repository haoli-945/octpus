package com.octpus.core.exception;

import lombok.Getter;

/**
 * 八爪鱼统一异常。
 *
 * @author haoli.xu
 * @since 1.0.0
 */
@Getter
public class OctpusException extends RuntimeException {
    private final String code;

    public OctpusException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OctpusException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static final String ERR_METHOD_MISSING    = "100001";
    public static final String ERR_METHOD_NOT_FOUND  = "100002";
    public static final String ERR_PARAM_PARSE       = "100003";
    public static final String ERR_INVOKE_FAILED     = "100004";
}
