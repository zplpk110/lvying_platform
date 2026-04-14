package com.lvying.web;

import com.lvying.service.CollectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

  private final CollectionService collectionService;

  public record BulkRemindBody(@NotEmpty List<UUID> tourIds) {}

  @GetMapping("/overdue-balance")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public List<CollectionService.OverdueTour> overdue() {
    return collectionService.overdueBalanceList();
  }

  @PostMapping("/bulk-remind")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public CollectionService.BulkRemindResult bulk(@Valid @RequestBody BulkRemindBody body) {
    return collectionService.bulkRemind(body.tourIds());
  }
}
