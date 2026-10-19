package org.mizgir.telegram.auto.bot.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.MaintenanceEntity;
import org.mizgir.telegram.auto.bot.entity.MaintenanceEntity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

  @Query("select sum (maintenance.cost) from MaintenanceEntity maintenance where maintenance.auto = :auto")
  BigDecimal sumCostsByAuto(AutoEntity auto);

  @Query("""
      select sum (maintenance.cost) from MaintenanceEntity maintenance \
      where maintenance.auto = :auto and maintenance.creationDate between :startDate and :endDate"""
  )
  BigDecimal sumCostsByAutoBetweenCreationDate(AutoEntity auto, LocalDate startDate, LocalDate endDate);
}
