package com.lvying.web;

import com.lvying.domain.UserRole;
import com.lvying.mapper.UserMapper;
import com.lvying.web.dto.LoginResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 用户辅助接口：如记支出时「垫付人」下拉列表。 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserMapper userMapper;

  @GetMapping("/staff-options")
  @PreAuthorize("hasAnyRole('BOSS_FINANCE', 'SALES_GUIDE')")
  public List<LoginResponse.UserInfo> staffOptions() {
    return userMapper.selectByRoleOrderByName(UserRole.SALES_GUIDE.name()).stream()
        .map(u -> new LoginResponse.UserInfo(u.getId(), u.getName(), u.getPhone(), u.getRole()))
        .toList();
  }
}
