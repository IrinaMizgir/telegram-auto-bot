package org.mizgir.telegram.auto.bot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class для работы с {@link java.math.BigDecimal}.
 * @author Ирина Мизгир
 * @date 27.10.2026 20:55
 */
public final class BigDecimalUtils {

  /**
   * Создаст BigDecimal из double.
   * @param value Десятичное число.
   * @return Большое десятичное число.
   */
  public static BigDecimal valueOf(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
  }
}
