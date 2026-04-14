package com.lvying.web;

import com.lvying.security.AppUserDetails;
import com.lvying.service.ExpenseService;
import com.lvying.service.TourService;
import com.lvying.web.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 团期 REST：列表、新建、详情、记收款、加游客、封团、记支出（支出逻辑在 {@link com.lvying.service.ExpenseService}）。
 */
@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

  private final TourService tourService;
  private final ExpenseService expenseService;

  @GetMapping
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public List<TourListItem> list() {
    return tourService.listInProgress();
  }

  @PostMapping
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public TourDetailResponse create(@Valid @RequestBody CreateTourRequest req) {
    return tourService.create(req);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public TourDetailResponse detail(@PathVariable UUID id) {
    return tourService.getDetail(id);
  }

  @PostMapping("/{id}/incomes")
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public TourDetailResponse income(
      @PathVariable UUID id, @Valid @RequestBody RecordIncomeRequest req) {
    return tourService.recordIncome(id, req);
  }

  @PostMapping("/{id}/guests")
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public TourDetailResponse guests(@PathVariable UUID id, @Valid @RequestBody AddGuestRequest req) {
    return tourService.addGuest(id, req);
  }

  @PostMapping("/{id}/settle")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public TourDetailResponse settle(@PathVariable UUID id) {
    return tourService.settle(id);
  }

  @PostMapping("/{id}/expenses")
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public TourDetailResponse expense(
      @PathVariable UUID id,
      @Valid @RequestBody CreateExpenseRequest req,
      @AuthenticationPrincipal AppUserDetails user) {
    expenseService.create(id, req, user);
    return tourService.getDetail(id);
  }
}
