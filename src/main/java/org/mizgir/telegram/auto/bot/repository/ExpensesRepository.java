package org.mizgir.telegram.auto.bot.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.ExpensesEntity;
import org.mizgir.telegram.auto.bot.entity.ExpensesEntity.ExpensesStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными расходов.
 * @author Ирина Мизгир
 * @date 16.10.2026 21:45
 */
@Repository
public interface ExpensesRepository extends JpaRepository<ExpensesEntity, Integer> {

  ExpensesEntity getByAutoIdAndStatusNot(Integer autoId, ExpensesStatus status);

  Optional<ExpensesEntity> findByAutoIdAndStatusNot(Integer autoId, ExpensesStatus status);

  @Query("select sum (expenses.cost) from ExpensesEntity expenses where expenses.auto = :auto")
  BigDecimal sumCostsByAuto(AutoEntity auto);

  @Query("""
      select sum (expenses.cost) from ExpensesEntity expenses \
      where expenses.auto = :auto and expenses.creationDate between :startDate and :endDate"""
  )
  BigDecimal sumCostsByAutoBetweenCreationDate(AutoEntity auto, LocalDate startDate, LocalDate endDate);
}


