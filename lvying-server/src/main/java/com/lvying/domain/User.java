package com.lvying.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

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

  @Column(nullable = false, unique = true, length = 32)
  private String phone;

  @Column(nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false, length = 64)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private UserRole role;

  @Column(length = 64)
  private String bankName;

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
