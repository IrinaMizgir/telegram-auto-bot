package org.mizgir.telegram.auto.bot.service;

import lombok.RequiredArgsConstructor;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.exception.UserActionStateNotFoundException;
import org.mizgir.telegram.auto.bot.repository.UserActionStateRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с данными состояния действиями пользователя.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 15:54
 */
@Service
@RequiredArgsConstructor
public class UserActionStateService {

  private final UserActionStateRepository userActionStateRepository;

  public void changeActionTypeById(long telegramUserId, ActionType actionType) {
    userActionStateRepository.updateActionType(telegramUserId, actionType);

  }


  /**
   * Найдет тип действия пользователя по идентификатору.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @return Тип действия.
   */
  public ActionType findActionTypeById(long telegramUserId) {
    return userActionStateRepository.findById(telegramUserId)
        .map(UserActionStateEntity::getType)
        .orElseThrow(() -> new UserActionStateNotFoundException(telegramUserId));

  }


}
