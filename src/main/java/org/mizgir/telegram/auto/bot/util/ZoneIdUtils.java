package org.mizgir.telegram.auto.bot.util;

import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class для работы с часовыми поясами.
 *
 * @author Ирина Мизгир
 * @date 31.10.2026 18:28
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZoneIdUtils {

  /**
   * Часовой пояс Минск.
   */
  public static final ZoneId MINSK_ZONE_ID = ZoneId.of("Europe/Minsk");
}
