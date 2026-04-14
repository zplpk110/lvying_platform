package com.lvying.mapper;

import com.lvying.domain.TourGuest;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TourGuestMapper {

  int insert(TourGuest guest);

  List<TourGuest> selectByTourId(@Param("tourId") UUID tourId);
}
