package com.lvying.repo;

import com.lvying.domain.TourGuest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourGuestRepository extends JpaRepository<TourGuest, UUID> {}
