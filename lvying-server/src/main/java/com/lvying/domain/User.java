package com.lvying.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统用户（表 {@code users}），MyBatis 映射；无 JPA 关联。
 *
 * <p>角色见 {@link UserRole}；银行信息用于月结导出。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private UUID id;
  /** 登录账号，唯一。 */
  private String phone;
  /** BCrypt 哈希。 */
  private String passwordHash;
  private String name;
  private UserRole role;
  /** 开户行（可空）。 */
  private String bankName;
  /** 卡号后四位。 */
  private String bankAccountLast4;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
