package com.lvying.mapper.dto;

import com.lvying.domain.TourStatus;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/** 进行中团列表（含销售姓名，一次 JOIN 查询）。 */
@Data
public class TourListRow {
  private UUID id;
  private String tourCode;
  private String name;
  private LocalDate departureDate;
  private TourStatus status;
  private String salesName;
}
