package org.mizgir.telegram.auto.bot.exception;

/**
 * Исключение если не найден объект регистрации автомобиля.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 16:21
 */
public class AutoRegistrationNotFoundException extends RuntimeException {

  public AutoRegistrationNotFoundException(Long telegramUserId) {
    super("UserActionState with id " + telegramUserId + " not found");
  }
}
