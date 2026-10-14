package org.mizgir.telegram.auto.bot.exception;

/**
 * Исключение если не найден пользователь.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 16:06
 */
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long id) {
    super("User with id " + id + " not found");
  }
}
