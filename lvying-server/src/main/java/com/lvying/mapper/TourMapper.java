package com.lvying.mapper;

import com.lvying.domain.Tour;
import com.lvying.mapper.dto.TourListRow;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TourMapper {

  Tour selectById(@Param("id") UUID id);

  Tour selectByTourCode(@Param("tourCode") String tourCode);

  /** @param statusName 枚举名，如 {@code IN_PROGRESS} */
  List<Tour> selectByStatusOrderByDepartureDate(@Param("statusName") String statusName);

  List<TourListRow> selectInProgressWithSalesName();

  int insert(Tour tour);

  int update(Tour tour);
}
