package com.octpus.core.converter;

/**
 * 参数转换器接口 - 核心层定义，适配层实现。
 *
 * 设计思路：
 * - 核心层只定义接口，不关心具体实现
 * - Spring 适配器用 Jackson 实现
 * - 未来可扩展 Gson/Fastjson2 实现
 *
 * @author octpus
 * @since 1.0.0
 */
public interface ParamConverter {
    /**
     * 将原始数据转换为目标参数类型。
     *
     * @param data       原始数据（通常是 Map 或 JSON）
     * @param targetType 目标参数类型
     * @return 转换后的对象
     */
    <T> T convert(Object data, Class<T> targetType);
}
