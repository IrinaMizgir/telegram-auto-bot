package org.mizgir.telegram.auto.bot.repository;

import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.AutoEntity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными автомобиля.
 *
 * @author Ирина Мизгир
 * @date 07.10.2026 22:27
 */
@Repository
public interface AutoRepository extends JpaRepository<AutoEntity, Integer> {

  /**
   * Получит автомобиль по идентификатору пользователя.
   *
   * @param userId Идентификатор пользователя.
   * @return Автомобиль
   */
  AutoEntity getByUserId(Long userId);

  /**
   * Найдёт автомобиль по идентификатору пользователя.
   *
   * @param userId Идентификатор пользователя.
   * @return Опциональный автомобиль.
   */
  Optional<AutoEntity> findByUserId(Long userId);

  /**
   * Найдёт автомобиль по идентификатору пользователя и статусу.
   *
   * @param userId Идентификатор пользователя.
   * @param status Статус.
   * @return Опциональный автомобиль.
   */
  Optional<AutoEntity> findByUserIdAndStatus(Long userId, RegistrationStatus status);

}
