package com.lvying.web;

import com.lvying.security.AppUserDetails;
import com.lvying.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/boss")
  @PreAuthorize("hasRole('BOSS_FINANCE')")
  public DashboardService.BossHomeResponse boss() {
    return dashboardService.bossHome();
  }

  @GetMapping("/staff")
  @PreAuthorize("hasRole('SALES_GUIDE')")
  public DashboardService.StaffHomeResponse staff(@AuthenticationPrincipal AppUserDetails user) {
    return dashboardService.staffHome(user.getId());
  }
}
