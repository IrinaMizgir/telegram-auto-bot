package org.mizgir.telegram.auto.bot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity.RefuelStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.model.RefuelSummary;
import org.mizgir.telegram.auto.bot.repository.RefuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с заправкой автомобиля.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 15:24
 */
@Service
@RequiredArgsConstructor
public class RefuelService {

  private final AutoService autoService;

  private final RefuelRepository refuelRepository;

  private final UserActionStateService userActionStateService;

  /**
   * Создание новой записи заправки.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат новый процесс заправки.
   */
  @Transactional
  public boolean create(Long telegramUserId) {
    Optional<AutoEntity> autoEntity = autoService.findByUserId(telegramUserId);
    if (autoEntity.isEmpty()) {
      return false;
    }
    userActionStateService.changeActionTypeById(telegramUserId, ActionType.ENTER_REFUEL);
    autoEntity.ifPresent(this::upsertRefuel);

    return true;
  }

  /**
   * Создание записи о заправке для указанного автомобиля.
   *
   * @param autoEntity Автомобиль, для которого создаётся запись о заправке.
   */
  private void upsertRefuel(AutoEntity autoEntity) {
    refuelRepository.findByAutoIdAndStatusNot(autoEntity.getId(), RefuelStatus.FINISHED)
        .ifPresentOrElse(
            refuelEntity -> onUpsertOnFindRefuel(refuelEntity, autoEntity),
            () -> create(autoEntity)
        );
  }

  /***
   * Обрабатывает найденную существующую запись о заправке.
   *
   * @param refuelEntity Найденная существующая заправка.
   * @param autoEntity Автомобиль, для которого создаётся запись о заправке.
   */
  private void onUpsertOnFindRefuel(RefuelEntity refuelEntity, AutoEntity autoEntity) {
    if (!RefuelStatus.ENTER_RUN.equals(refuelEntity.getStatus())) {
      refuelRepository.delete(refuelEntity);
      create(autoEntity);
    }
  }

  /**
   * Создаёт новую запись о заправке для указанного автомобиля и сохраняет её в репозитории.
   * @param autoEntity Автомобиль, для которого создаётся запись о заправке.
   */
  private void create(AutoEntity autoEntity) {
    RefuelEntity refuelEntity = new RefuelEntity();
    refuelEntity.setAuto(autoEntity);
    refuelRepository.save(refuelEntity);
  }

  /**
   * Ввод информации заправки.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данные заправки в виде текста.
   * @return Результат.
   */
  public EnterResult enterRefuelData(Long telegramUserId, String text) {
    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    RefuelEntity refuelEntity = refuelRepository.getByAutoIdAndStatusNot(autoEntity.getId(), RefuelStatus.FINISHED);
    RefuelStatus status = refuelEntity.getStatus();
    EnterRefuelInformationResultType resultType = switch (status) {
      case ENTER_RUN -> {
        if (NumberUtils.isDigits(text)) {
          long run = Long.parseLong(text);
          if (run > autoEntity.getRun()) {
            refuelEntity.setDistance(run - autoEntity.getRun());
            refuelEntity.setRun(run);
            autoEntity.setRun(run);
            yield EnterRefuelInformationResultType.SUCCESS_ENTER_RUN;
          }
        }
        yield EnterRefuelInformationResultType.INCORRECT_RUN;
      }
      case ENTER_AMOUNT -> {
        if (NumberUtils.isParsable(text)) {
          double amount = Double.parseDouble(text);
          if (amount > 0) {
            refuelEntity.setAmount(amount);
            yield EnterRefuelInformationResultType.SUCCESS_ENTER_AMOUNT;
          }
        }
        yield EnterRefuelInformationResultType.INCORRECT_AMOUNT;
      }
      case ENTER_PRICE -> {
        if (NumberUtils.isParsable(text)) {
          double doublePrice = Double.parseDouble(text);
          if (doublePrice > 0) {

            BigDecimal price = BigDecimal.valueOf(doublePrice).setScale(2, RoundingMode.HALF_UP);
            refuelEntity.setPrice(price);

            BigDecimal costs = BigDecimal.valueOf(refuelEntity.getAmount()).setScale(2, RoundingMode.HALF_UP)
                .multiply(price)
                .setScale(2, RoundingMode.HALF_UP);

            refuelEntity.setCost(costs);

            yield EnterRefuelInformationResultType.SUCCESS;
          }
        }
        yield EnterRefuelInformationResultType.INCORRECT_PRICE;
      }
      default -> throw new RuntimeException("Incorrect auto registration status");
    };
    if (resultType.success) {
      refuelEntity.setAuto(autoEntity);
      RefuelStatus nextStatus = RefuelStatus.getNextStatus(status);
      refuelEntity.setStatus(nextStatus);
      refuelRepository.save(refuelEntity);
      if (EnterRefuelInformationResultType.SUCCESS.equals(resultType)) {
        var refuelInfoStringBuilder = new StringBuilder();
        if (refuelEntity.getDistance() != null) {
          refuelInfoStringBuilder.append("\n\uD83D\uDEE3 Пробег:  ")
              .append(refuelEntity.getDistance())
              .append(" км.\n");
        }
        if (refuelEntity.getAmount() != null) {
          refuelInfoStringBuilder.append("⛽️ Залито:  ")
              .append(refuelEntity.getAmount())
              .append(" л.\n");
        }
        if (refuelEntity.getCost() != null) {
          refuelInfoStringBuilder.append("\uD83D\uDCB0 Сумма: ")
              .append(refuelEntity.getCost())
              .append(" BYN.");

        }
        return new EnterResult(true, resultType.message + refuelInfoStringBuilder);
      }

    }

    return new EnterResult(resultType.success, resultType.message);
  }

  /**
   * Получение суммарной информации о расходах на топлива.
   * @param autoEntity Сущность автомобиля.
   * @return Суммарная информация о расходах на топлива.
   */
  public RefuelSummary getRefuelSummaryByAuto(AutoEntity autoEntity) {
    return refuelRepository.calculateSummary(autoEntity);
  }

  /**
   * Получение суммарной информации о расходах на топлива за период.
   * @param autoEntity Сущность автомобиля.
   * @return Суммарная информация о расходах на топлива за период.
   */
  public RefuelSummary getRefuelSummaryByAutoAndPeriod(AutoEntity autoEntity, LocalDate startDate, LocalDate endDate) {
    return refuelRepository.sumCostsByAutoBetweenCreationDate(autoEntity, startDate, endDate);
  }

  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  public enum EnterRefuelInformationResultType {
    /**
     * Успешно введено количество литров.
     */
    SUCCESS_ENTER_RUN(true, "⛽️ Сколько топлива залили?\n"
        + "\n"
        + "Введите количество литров, например: 2.5 или 40.\n"
        + "\n"
        + "\uD83D\uDC47 Напишите количество литров:"),
    /**
     * Успешно введена стоимость за литр топлива.
     */
    SUCCESS_ENTER_AMOUNT(true, "\uD83D\uDCB5 Цена за литр\n"
        + "\n"
        + "Укажите стоимость одного литра топлива в BYN.\n"
        + "Например: 2.75.\n"
        + "\n"
        + "\uD83D\uDC47 Напишите цену за литр:"),
    /**
     * Успешно.
     */
    SUCCESS(true, "✅ Заправка добавлена"),

    /**
     * Некорректный пробег.
     */
    INCORRECT_RUN(false, "\uD83E\uDD14Кажется, пробег введён неверно\n"
        + "\n"
        + "Введите число километров, например: 608 или 120500."),

    /**
     * Некорректное количество литров.
     */
    INCORRECT_AMOUNT(false, "\uD83E\uDD14Кажется, количество топлива введено неверно\n"
        + "\n"
        + "Введите число литров, например: 2.0 или 40."),

    /**
     * Некорректная цена за литр топлива.
     */
    INCORRECT_PRICE(false, """
        ⚠️ Не удалось сохранить стоимость
        
        Похоже, вы ввели некорректное значение.
        Укажите цену за литр числом в BYN — например: 2.75.
        
        \uD83D\uDC47 Попробуйте ещё раз:""");

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
