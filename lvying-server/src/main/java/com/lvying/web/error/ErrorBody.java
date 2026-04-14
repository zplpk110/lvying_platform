package com.lvying.web.error;

/** API 错误响应体（JSON），与成功时的 DTO 分离便于前端拦截器处理。 */
public record ErrorBody(String code, String message) {}
