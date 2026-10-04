package org.mizgir.telegram.auto.bot.model;

/**
 * Данные клавиатуры.
 *
 * @param data Дополнительные данные.
 * @param type Тип данных.
 * @author Ирина Мизгир
 * @date 04.10.2026 19:12
 */
public record KeyboardData(Type type, String data) {

  /**
   * Тип данных.
   */
  public enum Type {
    /**
     * Регистрация.
     */
    REGISTER,
  }
}
