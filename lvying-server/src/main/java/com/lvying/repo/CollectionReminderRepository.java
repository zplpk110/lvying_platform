package com.lvying.repo;

import com.lvying.domain.CollectionReminder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 催收日志持久化（MVP 无复杂查询）。 */
public interface CollectionReminderRepository extends JpaRepository<CollectionReminder, UUID> {}
