package com.lvying.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 催收留痕（表 {@code collection_reminders}）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionReminder {

  private UUID id;
  private UUID tourId;
  private String channel;
  private String payload;
  private LocalDateTime sentAt;
}
