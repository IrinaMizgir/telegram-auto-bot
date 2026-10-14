package org.mizgir.telegram.auto.bot.repository;

import org.mizgir.telegram.auto.bot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными пользователя.
 * @author Ирина Мизгир
 * @date 11.10.2026 16:00
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
