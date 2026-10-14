package org.mizgir.telegram.auto.bot.repository;

import org.mizgir.telegram.auto.bot.entity.AutoRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными регистрации автомобиля.
 *
 * @author Ирина Мизгир
 * @date 07.10.2026 22:27
 */
@Repository
public interface AutoRegistrationRepository extends JpaRepository<AutoRegistrationEntity, Long> {

}
