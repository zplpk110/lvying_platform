package com.lvying.web.dto;

import com.lvying.domain.TourStatus;
import java.time.LocalDate;
import java.util.UUID;

/** 进行中团列表行（轻量字段，避免序列化整实体）。 */
public record TourListItem(
    UUID id,
    String tourCode,
    String name,
    LocalDate departureDate,
    TourStatus status,
    String salesName) {}
