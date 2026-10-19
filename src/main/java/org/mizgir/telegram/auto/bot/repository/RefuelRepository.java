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

  RefuelEntity getByAutoIdAndStatusNot(Integer autoId, RefuelStatus status);

  Optional<RefuelEntity> findByAutoIdAndStatusNot(Integer autoId, RefuelStatus status);

  @Query("select new org.mizgir.telegram.auto.bot.model.RefuelSummary(sum(refuel.distance), sum(refuel.cost))"
      + "from RefuelEntity refuel where refuel.auto = :auto")
  RefuelSummary calculateSummary(AutoEntity auto);

  @Query("""
      select new org.mizgir.telegram.auto.bot.model.RefuelSummary(sum(refuel.distance), sum(refuel.cost))\
      from RefuelEntity refuel where refuel.auto = :auto and refuel.creationDate between :startDate and :endDate""")
  RefuelSummary sumCostsByAutoBetweenCreationDate(AutoEntity auto, LocalDate startDate, LocalDate endDate);
}
