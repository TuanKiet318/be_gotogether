package com.vn.gotogether.config;


import com.vn.gotogether.dto.ApiResponse;
import com.vn.gotogether.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Không bọc nếu đã là ApiResponse hoặc là lỗi
        Class<?> paramType = returnType.getParameterType();
        return !(paramType.equals(ApiResponse.class)
                || paramType.equals(ErrorResponse.class)
                || Resource.class.isAssignableFrom(paramType));
//        return !(paramType.equals(ApiResponse.class) || paramType.equals(ErrorResponse.class));
    }
    //
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof ApiResponse || body instanceof ErrorResponse) {
            return body;
        }
        if (body instanceof Resource) {
            return body;
        }


        if (body instanceof String) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(ApiResponse.success(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON processing error", e);
            }
        }

        return ApiResponse.success(body);
    }
}
