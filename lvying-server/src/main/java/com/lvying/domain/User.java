package com.lvying.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

/**
 * 系统用户（表 {@code users}）。
 *
 * <p>角色区分老板/财务与业务员/导游，见 {@link UserRole}。手机号用于登录；银行信息仅用于报销月结「代发清单」导出，
 * 不参与鉴权。
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** 登录账号，唯一。 */
  @Column(nullable = false, unique = true, length = 32)
  private String phone;

  /** BCrypt 哈希，不明文存密码。 */
  @Column(nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false, length = 64)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private UserRole role;

  /** 开户行名称（月结 CSV 用，可空）。 */
  @Column(length = 64)
  private String bankName;

  /** 银行卡号后四位（脱敏展示）。 */
  @Column(length = 8)
  private String bankAccountLast4;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "salesUser")
  @Builder.Default
  private List<Tour> toursOwned = new ArrayList<>();

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
