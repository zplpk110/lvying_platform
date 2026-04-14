package com.lvying.mapper;

import com.lvying.domain.User;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  User selectByPhone(@Param("phone") String phone);

  User selectById(@Param("id") UUID id);

  /** @param roleName 枚举名，如 {@code SALES_GUIDE} */
  List<User> selectByRoleOrderByName(@Param("roleName") String roleName);

  int insert(User user);

  int update(User user);

  long count();
}
