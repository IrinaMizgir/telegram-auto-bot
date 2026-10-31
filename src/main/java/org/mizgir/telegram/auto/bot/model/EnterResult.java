package org.mizgir.telegram.auto.bot.model;

import java.util.Collections;
import java.util.Set;
import org.mizgir.telegram.auto.bot.model.KeyboardData.KeyboardType;

/**
 * Введенная информация.
 *
 * @param result Результат.
 * @param message Сообщение.
 * @param types Типы кнопок.
 * @author Ирина Мизгир
 * @date 12.10.2026 21:59
 */
public record EnterResult(boolean result, String message, Set<KeyboardType> types) {

  public EnterResult(boolean result, String message) {
    this(result, message, Collections.emptySet());
  }
}
