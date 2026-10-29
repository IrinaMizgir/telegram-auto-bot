package org.mizgir.telegram.auto.bot.util;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utility class для работы с числами.
 * @author Ирина Мизгир
 * @date 29.10.2026 19:05
 */
public final class NumberOps {

  /**
   * Проверит, содержит ли строка только числа.
   * @param str Строка.
   * @return Результат проверки.
   */
  public static boolean isNotDigits(final String str) {
    return !NumberUtils.isDigits(str);
  }

  /**
   * Проверит, можно ли разобрать строку в число (десятичное или целое).
   * @param str Строка.
   * @return Результат проверки.
   */
  public static boolean isNotParsable(final String str) {
    return !NumberUtils.isParsable(str);
  }
}
