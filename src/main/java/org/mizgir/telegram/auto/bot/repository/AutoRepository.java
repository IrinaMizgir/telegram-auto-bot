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

  AutoEntity getByUserId(Long userId);

  Optional<AutoEntity> findByUserId(Long userId);

  Optional<AutoEntity> findByUserIdAndStatus(Long userId, RegistrationStatus status);

}
