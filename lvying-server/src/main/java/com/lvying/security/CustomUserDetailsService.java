package com.lvying.security;

import com.lvying.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
