package com.lvying.mapper;

import com.lvying.domain.CollectionReminder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CollectionReminderMapper {

  int insert(CollectionReminder reminder);
}
