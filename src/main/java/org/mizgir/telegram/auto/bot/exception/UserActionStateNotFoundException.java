package org.mizgir.telegram.auto.bot.exception;

/**
 * Исключение если не найден состояния действия пользователя.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 16:06
 */
public class UserActionStateNotFoundException extends RuntimeException {

  public UserActionStateNotFoundException(Long telegramUserId) {
    super("UserActionState with id " + telegramUserId + " not found");
  }
}
