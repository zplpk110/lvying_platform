package com.lvying.repo;

import com.lvying.domain.TourGuest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 团内游客，独立仓储便于加人时不必级联整团保存。 */
public interface TourGuestRepository extends JpaRepository<TourGuest, UUID> {}
