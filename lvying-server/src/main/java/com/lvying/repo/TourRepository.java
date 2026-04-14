package com.lvying.repo;

import com.lvying.domain.Tour;
import com.lvying.domain.TourStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, UUID> {
  Optional<Tour> findByTourCode(String tourCode);

  @EntityGraph(attributePaths = {"salesUser"})
  List<Tour> findByStatusOrderByDepartureDateAsc(TourStatus status);
}
