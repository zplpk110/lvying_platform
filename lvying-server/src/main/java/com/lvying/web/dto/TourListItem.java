package com.lvying.web.dto;

import com.lvying.domain.TourStatus;
import java.time.LocalDate;
import java.util.UUID;

public record TourListItem(
    UUID id,
    String tourCode,
    String name,
    LocalDate departureDate,
    TourStatus status,
    String salesName) {}
