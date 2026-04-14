package com.lvying.web;

import com.lvying.domain.UserRole;
import com.lvying.repo.UserRepository;
import com.lvying.web.dto.LoginResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;

  @GetMapping("/staff-options")
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public List<LoginResponse.UserInfo> staffOptions() {
    return userRepository.findByRoleOrderByNameAsc(UserRole.SALES_GUIDE).stream()
        .map(u -> new LoginResponse.UserInfo(u.getId(), u.getName(), u.getPhone(), u.getRole()))
        .toList();
  }
}
