package com.lvying.repo;

import com.lvying.domain.CollectionReminder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionReminderRepository extends JpaRepository<CollectionReminder, UUID> {}
