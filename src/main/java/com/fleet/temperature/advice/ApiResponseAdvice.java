package com.fleet.temperature.advice;

import com.fleet.temperature.dto.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final String[] IGNORED_URIS = { "swagger", "api-docs", "/devcode/", "actuator" };

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse httpResponse) {
        
        if (body instanceof ApiResponse || StringUtils.containsAny(request.getURI().getPath(), IGNORED_URIS)) {
            return body;
        }
        
        if (body == null) {
            return ApiResponse.success(null, "Operation completed successfully");
        }
        
        return ApiResponse.success(body, "Operation completed successfully");
    }
}
