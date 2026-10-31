package org.mizgir.telegram.auto.bot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Данные клавиатуры.
 *
 * @param keyboardType Тип данных.
 * @param data Дополнительные данные.
 * @author Ирина Мизгир
 * @date 04.10.2026 19:12
 */
public record KeyboardData(KeyboardType keyboardType, String data) {

  /**
   * Тип данных.
   */

  @RequiredArgsConstructor
  public enum KeyboardType {
    /**
     * Регистрация.
     */
    REGISTER("🚗 Создать профиль автомобиля"),

    /**
     * Информация за период.
     */
    INFO_BY_PERIOD("Период"),

    /**
     * Информация за год.
     */
    INFO_BY_YEAR("Год"),

    /**
     * Информация за месяц.
     */
    INFO_BY_MONTH("Месяц"),

    /**
     * Информация за день.
     */
    INFO_BY_DAY("День");

    /**
     * Сообщение.
     */
    @Getter
    private final String message;
  }
}
