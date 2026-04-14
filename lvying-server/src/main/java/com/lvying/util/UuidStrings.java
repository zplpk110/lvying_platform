package com.lvying.util;

import java.util.UUID;

/** UUID 与无连字符 32 位十六进制字符串互转（库表、JWT subject、JSON 统一口径）。 */
public final class UuidStrings {

  private UuidStrings() {}

  /** 无连字符，小写十六进制，长度 32。 */
  public static String compact(UUID uuid) {
    return uuid.toString().replace("-", "");
  }

  /**
   * 解析 UUID：支持标准 36 位带连字符，或 32 位十六进制（大小写均可）。
   *
   * @throws IllegalArgumentException 非法格式
   */
  public static UUID parseLenient(String s) {
    if (s == null) {
      throw new IllegalArgumentException("uuid is null");
    }
    s = s.trim();
    if (s.isEmpty()) {
      throw new IllegalArgumentException("uuid is blank");
    }
    try {
      return UUID.fromString(s);
    } catch (IllegalArgumentException ignored) {
      if (s.length() == 32 && s.indexOf('-') < 0) {
        return UUID.fromString(
            s.replaceFirst(
                "(?i)(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"));
      }
      throw new IllegalArgumentException("Invalid UUID: " + s);
    }
  }
}
