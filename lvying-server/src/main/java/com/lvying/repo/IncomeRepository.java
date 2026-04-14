package com.lvying.repo;

import com.lvying.domain.Income;
import com.lvying.domain.IncomeType;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncomeRepository extends JpaRepository<Income, UUID> {

  @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i")
  BigDecimal sumAllAmount();

  @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.tour.id = :tourId")
  BigDecimal sumAmountByTourId(@Param("tourId") UUID tourId);

  @Query(
      "SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.tour.id = :tourId AND i.type = :type")
  BigDecimal sumAmountByTourIdAndType(
      @Param("tourId") UUID tourId, @Param("type") IncomeType type);
}
