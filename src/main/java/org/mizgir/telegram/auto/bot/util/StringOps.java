package org.mizgir.telegram.auto.bot.util;

import org.apache.commons.lang3.Strings;

/**
 * Utility class для работы со строками.
 * @author Ирина Мизгир
 * @date 29.10.2026 19:20
 */
public final class StringOps {

  /**
   * Заменяет запятые в строке на точку.
   * @param str Строка.
   * @return Строку с точкой.
   */
  public static String replaceCommaToDot(String str) {
    return Strings.CI.replace(str, ",", ".");
  }
}
