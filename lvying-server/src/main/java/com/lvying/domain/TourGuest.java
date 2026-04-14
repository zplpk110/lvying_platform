package com.lvying.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 团内游客（表 {@code tour_guests}）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourGuest {

  private UUID id;
  private UUID tourId;
  private String name;
  private String phoneMasked;
  private String phoneRaw;
  @Builder.Default private BigDecimal balanceDue = BigDecimal.ZERO;
  private LocalDateTime createdAt;
}
