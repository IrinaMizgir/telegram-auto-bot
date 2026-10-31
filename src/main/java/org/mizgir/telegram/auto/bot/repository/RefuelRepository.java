package org.mizgir.telegram.auto.bot.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity.RefuelStatus;
import org.mizgir.telegram.auto.bot.model.RefuelSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными заправки автомобиля.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 15:19
 */
@Repository
public interface RefuelRepository extends JpaRepository<RefuelEntity, Integer> {

  /**
   * Получит объект заправки по идентификатору автомобиля и статусу.
   *
   * @param autoId Идентификатор автомобиля.
   * @param status Статус.
   * @return Объект заправки.
   */
  RefuelEntity getByAutoIdAndStatusNot(Integer autoId, RefuelStatus status);

  /**
   * Найдёт объект заправки по идентификатору автомобиля и статусу.
   *
   * @param autoId Идентификатор автомобиля.
   * @param status Статус.
   * @return Опциональный объект заправки.
   */
  Optional<RefuelEntity> findByAutoIdAndStatusNot(Integer autoId, RefuelStatus status);

  /**
   * Рассчитает суммарное сведение о заправке.
   *
   * @param auto Автомобиль.
   * @return Суммарное сведение.
   */
  @Query("select new org.mizgir.telegram.auto.bot.model.RefuelSummary(sum(refuel.distance), sum(refuel.cost))"
      + "from RefuelEntity refuel where refuel.auto = :auto")
  RefuelSummary calculateSummary(AutoEntity auto);

  /**
   * Посчитает сумму расходов за заправку по автомобилю за период.
   *
   * @param auto Автомобиль.
   * @param startDate Начало периода.
   * @param endDate Конец периода.
   * @return Суммарное сведение.
   */
  @Query("""
      select new org.mizgir.telegram.auto.bot.model.RefuelSummary(sum(refuel.distance), sum(refuel.cost))\
      from RefuelEntity refuel where refuel.auto = :auto and refuel.creationDate between :startDate and :endDate""")
  RefuelSummary sumCostsByAutoBetweenCreationDate(AutoEntity auto, LocalDate startDate, LocalDate endDate);
}
