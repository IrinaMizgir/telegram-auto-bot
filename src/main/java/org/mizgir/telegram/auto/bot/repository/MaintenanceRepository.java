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
 *
 * @author Ирина Мизгир
 * @date 12.10.2026 21:46
 */
@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceEntity, Integer> {

  /**
   * Получит объект "СО" по идентификатору автомобиля и статусу.
   *
   * @param autoId Идентификатор автомобиля.
   * @param status Статус.
   * @return Объект "СО".
   */
  MaintenanceEntity getByAutoIdAndStatusNot(Integer autoId, MaintenanceStatus status);

  /**
   * Найдёт объект "СО" по идентификатору автомобиля и статусу.
   *
   * @param autoId Идентификатор автомобиля.
   * @param status Статус.
   * @return Опциональный объект "СО".
   */
  Optional<MaintenanceEntity> findByAutoIdAndStatusNot(Integer autoId, MaintenanceStatus status);

  /**
   * Посчитает сумму расходов за "Со" по автомобилю.
   *
   * @param auto Автомобиль.
   * @return Большое десятичное число.
   */
  @Query("select sum (maintenance.cost) from MaintenanceEntity maintenance where maintenance.auto = :auto")
  BigDecimal sumCostsByAuto(AutoEntity auto);

  /**
   * Посчитает сумму расходов за "Со" по автомобилю за период.
   *
   * @param auto Автомобиль.
   * @param startDate Начало периода.
   * @param endDate Конец периода.
   * @return Большое десятичное число.
   */
  @Query("""
      select sum (maintenance.cost) from MaintenanceEntity maintenance \
      where maintenance.auto = :auto and maintenance.creationDate between :startDate and :endDate"""
  )
  BigDecimal sumCostsByAutoBetweenCreationDate(AutoEntity auto, LocalDate startDate, LocalDate endDate);
}
