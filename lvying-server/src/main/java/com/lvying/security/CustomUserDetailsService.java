package com.lvying.security;

import com.lvying.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 按「手机号」加载用户（与表单登录、JWT 内 claim 一致）。
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
    return userRepository
        .findByPhone(phone)
        .map(
            u ->
                new AppUserDetails(
                    u.getId(), u.getPhone(), u.getPasswordHash(), u.getRole(), u.getName()))
        .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
  }
}
