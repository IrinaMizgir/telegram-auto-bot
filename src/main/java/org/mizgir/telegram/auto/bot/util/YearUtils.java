package org.mizgir.telegram.auto.bot.util;

import java.time.Year;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class для работы с годами.
 *
 * @author Ирина Мизгир
 * @date 31.10.2026 18:18
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YearUtils {

  /**
   * Вернет текущий год по поясу Европа/Минск.
   *
   * @return Текущий год по поясу Европа/Минск.
   */
  public static Year now() {
    return Year.now(ZoneIdUtils.MINSK_ZONE_ID);
  }
}
