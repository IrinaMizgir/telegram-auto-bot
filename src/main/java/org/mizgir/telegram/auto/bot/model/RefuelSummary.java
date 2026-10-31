package org.mizgir.telegram.auto.bot.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Суммарная информация о заправке.
 *
 * @param costByKm Цена за км.
 * @param costSum Общая сумма.
 * @author Ирина Мизгир
 * @date 17.10.2026 21:16
 */
public record RefuelSummary(Long costByKm, BigDecimal costSum) {

  /**
   * Расчёт среднего расхода.
   *
   * @return Средний расход.
   */
  public BigDecimal calculateAverageRefuelCost() {
    if (costByKm == null || costSum == null) {
      return BigDecimal.ZERO;
    }
    return costSum.divide(BigDecimal.valueOf(costByKm), 2, RoundingMode.HALF_UP);
  }

  /**
   * Вернет общую сумму или 0.
   *
   * @return Общая сумма или 0.
   */
  public BigDecimal getCostsOrZero() {
    if (costSum == null) {
      return BigDecimal.ZERO;
    }
    return costSum;
  }
}
