package com.vn.gotogether.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.gotogether.dto.ApiResponse;
import com.vn.gotogether.exception.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {

        Class<?> paramType = returnType.getParameterType();

        return !(ApiResponse.class.isAssignableFrom(paramType)
                || ErrorResponse.class.isAssignableFrom(paramType)
                || Resource.class.isAssignableFrom(paramType)
                || byte[].class.isAssignableFrom(paramType)
                || String.class.isAssignableFrom(paramType)
                || ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)
                || StringHttpMessageConverter.class.isAssignableFrom(converterType));
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        String path = request.getURI().getPath();

        // Bỏ qua swagger / openapi
        if (path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")) {
            return body;
        }

        if (body instanceof ApiResponse || body instanceof ErrorResponse) {
            return body;
        }

        if (body instanceof Resource || body instanceof byte[]) {
            return body;
        }

        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(ApiResponse.success(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON processing error", e);
            }
        }

        return ApiResponse.success(body);
    }
}