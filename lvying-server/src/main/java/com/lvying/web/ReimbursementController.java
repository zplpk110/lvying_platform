package com.lvying.web;

import com.lvying.security.AppUserDetails;
import com.lvying.service.ReimbursementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reimbursements")
@RequiredArgsConstructor
public class ReimbursementController {

  private final ReimbursementService reimbursementService;

  public record PartialRejectBody(@NotBlank String note) {}

  public record BatchPaidBody(@NotEmpty List<UUID> expenseIds, @NotBlank String batchRef) {}

  @GetMapping("/pending-by-tour")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public List<ReimbursementService.PendingGroup> pending() {
    return reimbursementService.pendingByTour();
  }

  @PostMapping("/{expenseId}/approve")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public void approve(
      @PathVariable UUID expenseId, @AuthenticationPrincipal AppUserDetails user) {
    reimbursementService.approve(expenseId, user.getId());
  }

  @PostMapping("/{expenseId}/partial-reject")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public void partialReject(
      @PathVariable UUID expenseId,
      @Valid @RequestBody PartialRejectBody body,
      @AuthenticationPrincipal AppUserDetails user) {
    reimbursementService.partialReject(expenseId, user.getId(), body.note());
  }

  @GetMapping("/batch-export")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public ReimbursementService.BatchExportResponse export(
      @RequestParam int year, @RequestParam int month) {
    return reimbursementService.batchExport(year, month);
  }

  @PostMapping("/mark-batch-paid")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public void markPaid(@Valid @RequestBody BatchPaidBody body) {
    reimbursementService.markBatchPaid(body.expenseIds(), body.batchRef());
  }
}
