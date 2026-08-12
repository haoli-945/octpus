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
    private final String desc;

    // ==================== 新版：枚举驱动 ====================

    /**
     * 仅传枚举，message 默认取 desc。
     */
    public OctpusException(OctpusErrorCode errorCode) {
        super(errorCode.getDesc());
        this.code = errorCode.getCode();
        this.desc = errorCode.getDesc();
    }

    /**
     * 枚举 + 动态补充信息（如具体接口名）。
     */
    public OctpusException(OctpusErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.desc = errorCode.getDesc();
    }

    /**
     * 枚举 + 动态补充信息 + 原始异常。
     */
    public OctpusException(OctpusErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.desc = errorCode.getDesc();
    }

    // ==================== 旧版：向后兼容（已废弃） ====================

    @Deprecated(forRemoval = true)
    public OctpusException(String code, String message) {
        super(message);
        this.code = code;
        this.desc = message;
    }

    @Deprecated(forRemoval = true)
    public OctpusException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.desc = message;
    }
}
