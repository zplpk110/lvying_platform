package com.lvying.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

/**
 * 催收动作留痕（表 {@code collection_reminders}）。
 *
 * <p>MVP 仅写入系统，{@code channel} 如 {@code SMS_STUB} 表示未接真实短信网关；后续可对接运营商/企微。
 */
@Entity
@Table(name = "collection_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionReminder {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tour_id", nullable = false)
  private Tour tour;

  @Column(nullable = false, length = 32)
  private String channel;

  @Column(nullable = false, length = 1024)
  private String payload;

  @Column(nullable = false)
  @Builder.Default
  private Instant sentAt = Instant.now();

  @PrePersist
  void prePersist() {
    if (sentAt == null) sentAt = Instant.now();
  }
}
