package com.lvying.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * REST 统一异常：业务码、校验错误、实体未找到分别映射 400/404。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorBody> business(BusinessException e) {
    if ("NOT_FOUND".equals(e.getCode())) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorBody(e.getCode(), e.getMessage()));
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorBody(e.getCode(), e.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorBody> badRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorBody("VALIDATION", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException e) {
    String msg =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .orElse("参数错误");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody("VALIDATION", msg));
  }
}
