package com.lvying.web.error;

import lombok.Getter;

/**
 * 可预期业务异常，由 {@link GlobalExceptionHandler} 统一转为 HTTP 400与 {@link ErrorBody}（含 {@code code} 供前端区分，如
 * {@code OVERSPEND_GUARD}）。
 */
@Getter
public class BusinessException extends RuntimeException {

  private final String code;

  public BusinessException(String code, String message) {
    super(message);
    this.code = code;
  }
}
