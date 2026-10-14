package org.mizgir.telegram.auto.bot.repository;

import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Репозиторий для работы с данными состояния действиями пользователя.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 15:44
 */
@Repository
public interface UserActionStateRepository extends JpaRepository<UserActionStateEntity, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE UserActionStateEntity u SET u.type = :actionType WHERE u.id = :id")
  void updateActionType(@Param("id") long id, @Param("actionType") ActionType actionType);
}
