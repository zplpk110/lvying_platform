package com.lvying.web.error;

import jakarta.persistence.EntityNotFoundException;
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

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorBody> notFound(EntityNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorBody("NOT_FOUND", e.getMessage()));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorBody> business(BusinessException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorBody(e.getCode(), e.getMessage()));
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
