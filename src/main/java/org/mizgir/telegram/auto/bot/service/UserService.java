package org.mizgir.telegram.auto.bot.service;

import lombok.RequiredArgsConstructor;
import org.mizgir.telegram.auto.bot.entity.UserEntity;
import org.mizgir.telegram.auto.bot.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с данными пользователя.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 16:02
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /**
   * Создание нового пользователя.
   *
   * @param id       Идентификатор пользователя.
   * @param userName Имя пользователя.
   * @return Новый пользователь.
   */
  public UserEntity upsert(Long id, String userName) {
    var userEntity = new UserEntity();
    userEntity.setId(id);
    userEntity.setUserName(userName);

    return userRepository.save(userEntity);
  }

}
