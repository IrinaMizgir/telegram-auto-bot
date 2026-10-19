package org.mizgir.telegram.auto.bot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
          double doubleCost = Double.parseDouble(text);
          maintenanceEntity.setCost(BigDecimal.valueOf(doubleCost).setScale(2, RoundingMode.HALF_UP));
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
              .append("\n\uD83D\uDEE3Пробег: ")
              .append(autoEntity.getRun())
              .append(" км.");
        }
        if (StringUtils.isNoneEmpty(maintenanceEntity.getDescription())) {
          maintenanceInfoStringBuilder.append("\n\uD83D\uDEE0Выполненные работы: ")
              .append(maintenanceEntity.getDescription())
              .append(".");
        }
        if (maintenanceEntity.getCost() != null) {
          maintenanceInfoStringBuilder.append("\n\uD83D\uDCB5Стоимость:  ")
              .append(maintenanceEntity.getCost())
              .append(" BYN.");
        }

        return new EnterResult(true, resultType.message + maintenanceInfoStringBuilder);
      }
    }
    return new EnterResult(resultType.success, resultType.message);
  }

  /**
   * Получение общей суммы за СО.
   * @param autoEntity Сущность автомобиля.
   * @return Сумма расхода за СО.
   */
  public BigDecimal getSumByAuto(AutoEntity autoEntity) {

    BigDecimal bigDecimal = maintenanceRepository.sumCostsByAuto(autoEntity);
    if (bigDecimal == null) {
      return BigDecimal.ZERO;
    }
    return bigDecimal;
  }

  /**
   * Получение суммарной информации о расходах на СО за период.
   * @param autoEntity Сущность автомобиля.
   * @return Суммарная информация о расходах на СО за период.
   */
  public BigDecimal getMaintenanceSummaryByAutoAndPeriod(AutoEntity autoEntity, LocalDate startDate,
      LocalDate endDate) {
    BigDecimal bigDecimal = maintenanceRepository.sumCostsByAutoBetweenCreationDate(autoEntity, startDate, endDate);
    if (bigDecimal == null) {
      return BigDecimal.ZERO;
    }
    return bigDecimal;
  }

  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  public enum EnterMaintenanceInformationResultType {

    /**
     * Успешно введен пробег.
     */
    SUCCESS_ENTER_RUN(true, "\uD83D\uDEE0 Сервисные работы\n"
        + "\n"
        + "Введите краткое описание выполненных работ:\n"
        + "• что заменили\n"
        + "• что отремонтировали\n"
        + "• какие услуги оказали\n"
        + "\n"
        + "Примеры:\n"
        + "Замена масла\n"
        + "Замена тормозных колодок\n"
        + "Шиномонтаж\n"
        + "\n"
        + "\uD83D\uDC47 Напишите описание:"),

    /**
     * Успешно введена описание работ СО.
     */
    SUCCESS_ENTER_DESCRIPTION(true, """
        \uD83D\uDCB5 Стоимость сервисных работ
        
        Укажите сумму в белорусских рублях (BYN).
        Например: 50.00 или 50.
        
        \uD83D\uDC47 Напишите стоимость работ:"""),

    /**
     * Успешно.
     */
    SUCCESS(true, "✅ Сервисное обслуживание добавлено"),

    /**
     * Некорректный пробег.
     */
    INCORRECT_RUN(false, """
        \uD83E\uDD14Кажется, пробег введён неверно
        
        Введите число километров, например: 608 или 120500."""),

    /**
     * Некорректная стоимость СО.
     */
    INCORRECT_COST(false, "\uD83D\uDE42Похоже, стоимость не распозналась\n"
        + "\n"
        + "Введите сумму в BYN — просто число, больше нуля.\n"
        + "Например: 55.00 или 120.50.\n"
        + "\n"
        + "\uD83D\uDC47 Попробуем ещё раз?");

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


