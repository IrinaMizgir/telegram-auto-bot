package org.mizgir.telegram.auto.bot.util;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class для работы с датами.
 *
 * @author Ирина Мизгир
 * @date 31.10.2026 18:25
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDateUtils {

  /**
   * Вернет текущую дату по часовому поясу Европа/Минск.
   * @return Текущая дата по часовому поясу Европа/Минск
   */
  public static LocalDate now() {
    return LocalDate.now(ZoneIdUtils.MINSK_ZONE_ID);
  }
}
