package org.mizgir.telegram.auto.bot.repository;

import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.MaintenanceEntity;
import org.mizgir.telegram.auto.bot.entity.MaintenanceEntity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными СО.
 * @author Ирина Мизгир
 * @date 12.10.2026 21:46
 */
@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity, Integer> {

  MaintenanceEntity getByAutoIdAndStatusNot(Integer autoId, MaintenanceStatus status);

  Optional<MaintenanceEntity> findByAutoIdAndStatusNot(Integer autoId, MaintenanceStatus status);
}
