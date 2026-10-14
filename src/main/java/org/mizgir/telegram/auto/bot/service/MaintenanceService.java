package org.mizgir.telegram.auto.bot.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.MaintenanceEntity;
import org.mizgir.telegram.auto.bot.entity.MaintenanceEntity.MaintenanceStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.repository.MaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с СО.
 *
 * @author Ирина Мизгир
 * @date 12.10.2026 21:49
 */
@Service
@RequiredArgsConstructor
public class MaintenanceService {

  private final AutoService autoService;

  private final MaintenanceRepository maintenanceRepository;

  private final UserActionStateService userActionStateService;


  /**
   * Создание новой записи СО.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат новый процесс СО.
   */
  @Transactional
  public boolean create(Long telegramUserId) {
    Optional<AutoEntity> autoEntity = autoService.findByUserId(telegramUserId);
    if (autoEntity.isEmpty()) {
      return false;
    }
    userActionStateService.changeActionTypeById(telegramUserId, ActionType.ENTER_MAINTENANCE);
    autoEntity.ifPresent(this::upsertMaintenance);

    return true;
  }

  /**
   * Создание записи о СО.
   *
   * @param autoEntity Автомобиль, для которого создаётся запись о СО.
   */
  private void upsertMaintenance(AutoEntity autoEntity) {
    maintenanceRepository.findByAutoIdAndStatusNot(autoEntity.getId(), MaintenanceStatus.FINISHED)
        .ifPresentOrElse(maintenanceEntity -> onUpsertOnFindMaintenance(maintenanceEntity, autoEntity),
            () -> create(autoEntity));
  }

  /**
   * Обрабатывает найденную существующую запись о СО.
   * @param maintenanceEntity Найденная существующая запись о СО.
   * @param autoEntity Автомобиль, для которого создаётся запись о СО.
   */
  private void onUpsertOnFindMaintenance(MaintenanceEntity maintenanceEntity, AutoEntity autoEntity) {
    if (!MaintenanceStatus.ENTER_RUN.equals(maintenanceEntity.getStatus())) {
      maintenanceRepository.delete(maintenanceEntity);
      create(autoEntity);
    }
  }

  /**
   * Создаёт новую запись о СО для указанного автомобиля и сохраняет её в репозитории.
   *
   * @param autoEntity Автомобиль, для которого создаётся запись о СО.
   */
  private void create(AutoEntity autoEntity) {
    MaintenanceEntity maintenanceEntity = new MaintenanceEntity();
    maintenanceEntity.setAuto(autoEntity);
    maintenanceRepository.save(maintenanceEntity);
  }

  /**
   * Ввод информации заправки.
   *MaintenanceStatus
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данные заправки в виде текста.
   * @return Результат.
   */
  public EnterResult enterMaintenanceData(Long telegramUserId, String text) {

    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    MaintenanceEntity maintenanceEntity = maintenanceRepository.getByAutoIdAndStatusNot(autoEntity.getId(),
        MaintenanceStatus.FINISHED);
    MaintenanceStatus status = maintenanceEntity.getStatus();
    EnterMaintenanceInformationResultType resultType = switch (status) {
      case ENTER_RUN -> {
        if (NumberUtils.isDigits(text)) {
          long run = Long.parseLong(text);
          if (run > autoEntity.getRun()) {
            maintenanceEntity.setDistance(run - autoEntity.getRun());
            maintenanceEntity.setRun(run);
            autoEntity.setRun(run);
            yield EnterMaintenanceInformationResultType.SUCCESS_ENTER_RUN;
          }
        }
        yield EnterMaintenanceInformationResultType.INCORRECT_RUN;
      }
      case ENTER_DESCRIPTION -> {
        maintenanceEntity.setDescription(text);
        yield EnterMaintenanceInformationResultType.SUCCESS_ENTER_DESCRIPTION;
      }

      case ENTER_COST -> {
        if (NumberUtils.isParsable(text)) {
          maintenanceEntity.setCost(Double.parseDouble(text));
          yield EnterMaintenanceInformationResultType.SUCCESS;
        }
        yield EnterMaintenanceInformationResultType.INCORRECT_COST;
      }
      default -> throw new RuntimeException("Incorrect auto registration status");
    };

    if (resultType.success) {
      maintenanceEntity.setAuto(autoEntity);
      MaintenanceStatus nextStatus = MaintenanceStatus.getNextStatus(status);
      maintenanceEntity.setStatus(nextStatus);
      maintenanceRepository.save(maintenanceEntity);

      if (EnterMaintenanceInformationResultType.SUCCESS.equals(resultType)) {

        var maintenanceInfoStringBuilder = new StringBuilder();
        if (maintenanceEntity.getRun() != null) {
          maintenanceInfoStringBuilder
              .append("\nПробег: ")
              .append(autoEntity.getRun())
              .append(" км.");
        }
        if (StringUtils.isNoneEmpty(maintenanceEntity.getDescription())) {
          maintenanceInfoStringBuilder.append("\nВыполнены работы: ")
              .append(maintenanceEntity.getDescription())
              .append(".");
        }
        if (maintenanceEntity.getCost() != null) {
          maintenanceInfoStringBuilder.append("\nСумма: ")
              .append(maintenanceEntity.getCost())
              .append(" BYN.");
        }

        return new EnterResult(true, resultType.message + maintenanceInfoStringBuilder);
      }
    }

    if (resultType.success) {
      maintenanceEntity.setAuto(autoEntity);
      MaintenanceStatus nextStatus = MaintenanceStatus.getNextStatus(status);
      maintenanceEntity.setStatus(nextStatus);
      maintenanceRepository.save(maintenanceEntity);
    }
    return new EnterResult(resultType.success, resultType.message);
  }


  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  public enum EnterMaintenanceInformationResultType {

    /**
     * Успешно введен пробег.
     */
    SUCCESS_ENTER_RUN(true, "Напишите какие работы были сделаны.(Например, замена масла."),

    /**
     * Успешно введена описание работ СО.
     */
    SUCCESS_ENTER_DESCRIPTION(true, "Введите стоимость работ."),

    /**
     * Успешно.
     */
    SUCCESS(true, "Сервисное обслуживание добавлено."),

    /**
     * Некорректный пробег.
     */
    INCORRECT_RUN(false, "Некорректный пробег."),

    /**
     * Некорректная стоимость СО.
     */
    INCORRECT_COST(false, "Неверная стоимость.");

    /**
     * Признак успешности.
     */
    private final boolean success;

    /**
     * Сообщение.
     */
    private final String message;

  }


}


