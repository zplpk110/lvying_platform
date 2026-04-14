package com.lvying.mapper;

import com.lvying.domain.Income;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IncomeMapper {

  int insert(Income income);

  BigDecimal sumAllAmount();

  BigDecimal sumAmountByTourId(@Param("tourId") UUID tourId);

  BigDecimal sumAmountByTourIdAndType(
      @Param("tourId") UUID tourId, @Param("typeName") String typeName);
}
