package com.octpus.spring.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octpus.core.converter.ParamConverter;

/**
 * 基于 Jackson 的参数转换器实现。
 *
 * @author haoli.xu
 * @since 1.0.0
 */
public class JacksonParamConverter implements ParamConverter {

    private final ObjectMapper objectMapper;

    public JacksonParamConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T convert(Object data, Class<T> targetType) {
        return objectMapper.convertValue(data, targetType);
    }
}
