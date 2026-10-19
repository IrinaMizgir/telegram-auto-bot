package org.mizgir.telegram.auto.bot.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.repository.AutoRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с автомобилями.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 16:18
 */
@Service
@RequiredArgsConstructor
public class AutoService {

  private final AutoRepository autoRepository;

  private final UserService userService;

  /**
   * Создание нового автомобиля.
   *
   * @param userId Идентификатор пользователя.
   * @return Новый автомобиль.
   */
  public AutoEntity newAuto(long userId) {
    AutoEntity autoEntity = new AutoEntity();
    autoEntity.setUser(userService.getUserById(userId));

    return autoEntity;
  }

  /**
   * Получение автомобиля по идентификатору пользователя.
   *
   * @param userId Идентификатор пользователя.
   * @return Автомобиль.
   */
  public AutoEntity getByUserId(long userId) {
    return autoRepository.getByUserId(userId);
  }

  /**
   * Получение автомобиля по идентификатору пользователя.
   *
   * @param userId Идентификатор пользователя.
   * @return Автомобиль.
   */
  public Optional<AutoEntity> findByUserId(long userId) {
    return autoRepository.findByUserId(userId);
  }
}
