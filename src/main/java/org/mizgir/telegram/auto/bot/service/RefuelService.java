package org.mizgir.telegram.auto.bot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity.RefuelStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.factory.DataResultFactory;
import org.mizgir.telegram.auto.bot.model.DataResult;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.model.RefuelSummary;
import org.mizgir.telegram.auto.bot.repository.RefuelRepository;
import org.mizgir.telegram.auto.bot.util.BigDecimalUtils;
import org.mizgir.telegram.auto.bot.util.NumberOps;
import org.mizgir.telegram.auto.bot.util.StringOps;
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

  private final DataResultFactory dataResultFactory;

  /**
   * Создание новой записи заправки.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат новый процесс заправки.
   */
  @Transactional
  public DataResult create(Long telegramUserId) {
    Optional<AutoEntity> autoEntity = autoService.findByUserIdAndFinished(telegramUserId);
    if (autoEntity.isEmpty()) {
      return dataResultFactory.createInvalid();
    }
    userActionStateService.changeActionTypeById(telegramUserId, ActionType.ENTER_REFUEL);
    autoEntity.ifPresent(this::upsertRefuel);

    return dataResultFactory.createValid("""
        ⛽Заправка автомобиля
        
        Введите текущий пробег автомобиля в километрах.
        Например: 120500 или 608.
        
        \uD83D\uDC47 Напишите пробег:""");
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
    EnterRefuelInformationResultType resultType = trySetNewData(text, status, autoEntity,
        refuelEntity);
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

  private @NonNull EnterRefuelInformationResultType trySetNewData(String text, RefuelStatus status,
      AutoEntity autoEntity, RefuelEntity refuelEntity) {
    return switch (status) {
      case ENTER_RUN -> {
        if (NumberOps.isNotDigits(text)) {
          yield EnterRefuelInformationResultType.INCORRECT_RUN_NOT_NUMBER;
        }
        long run = Long.parseLong(text);
        if (run < autoEntity.getRun()) {
          yield EnterRefuelInformationResultType.INCORRECT_RUN_LESS;
        }
        refuelEntity.setDistance(run - autoEntity.getRun());
        refuelEntity.setRun(run);
        autoEntity.setRun(run);
        yield EnterRefuelInformationResultType.SUCCESS_ENTER_RUN;

      }
      case ENTER_AMOUNT -> {

        String normalizedText = StringOps.replaceCommaToDot(text);
        if (NumberOps.isNotParsable(normalizedText)) {
          yield EnterRefuelInformationResultType.INCORRECT_AMOUNT_NOT_NUMBER;
        }
        double amount = Double.parseDouble(normalizedText);
        if (amount <= 0) {
          yield EnterRefuelInformationResultType.INCORRECT_AMOUNT_NEGATIVE;
        }
        if (amount > 0) {
          refuelEntity.setAmount(amount);
        }
        yield EnterRefuelInformationResultType.SUCCESS_ENTER_AMOUNT;
      }
      case ENTER_PRICE -> {
        String normalizedText = StringOps.replaceCommaToDot(text);
        if (NumberOps.isNotParsable(normalizedText)) {
          yield EnterRefuelInformationResultType.INCORRECT_PRICE_NOT_NUMBER;
        }

        double doublePrice = Double.parseDouble(normalizedText);
        if (doublePrice <= 0) {
          yield EnterRefuelInformationResultType.INCORRECT_PRICE_NEGATIVE;
        }
        if (doublePrice > 0) {

          BigDecimal price = BigDecimalUtils.valueOf(doublePrice);
          refuelEntity.setPrice(price);

          BigDecimal costs = BigDecimalUtils.valueOf(refuelEntity.getAmount())
              .multiply(price)
              .setScale(2, RoundingMode.HALF_UP);

          refuelEntity.setCost(costs);

        }
        yield EnterRefuelInformationResultType.SUCCESS;
      }
      default -> throw new RuntimeException("Incorrect auto registration status");
    };
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
  private enum EnterRefuelInformationResultType {
    /**
     * Успешно введено количество литров.
     */
    SUCCESS_ENTER_RUN(true, """
        ⛽️ Сколько топлива залили?
        
        Введите количество литров, например: 2.5 или 40.
        
        \uD83D\uDC47 Напишите количество литров:"""),
    /**
     * Успешно введена стоимость за литр топлива.
     */
    SUCCESS_ENTER_AMOUNT(true, """
        \uD83D\uDCB5 Цена за литр
        
        Укажите стоимость одного литра топлива в BYN.
        Например: 2.75.
        
        \uD83D\uDC47 Напишите цену за литр:"""),
    /**
     * Успешно.
     */
    SUCCESS(true, "✅ Заправка добавлена"),

    /**
     * Некорректный пробег.
     */
    INCORRECT_RUN_NOT_NUMBER(false, """
        \uD83E\uDD14Кажется, пробег введён неверно
        
        
        Введите число километров, например: 608 или 120500."""),

    /**
     * Некорректный пробег (меньше чем предыдущий).
     */
    INCORRECT_RUN_LESS(false, """
        \uD83E\uDD14Кажется, пробег введён неверно
        
        Введенный пробег меньше чем предыдущий.
        Введите число километров, например: 608 или 120500."""),

    /**
     * Некорректное количество литров (не число).
     */
    INCORRECT_AMOUNT_NOT_NUMBER(false, """
        \uD83E\uDD14Кажется, количество топлива введено неверно
        
        не является числом
        Введите число литров, например: 2.0 или 40."""),

    /**
     * Некорректное количество литров (число равно 0 или негативное).
     */
    INCORRECT_AMOUNT_NEGATIVE(false, """
        \uD83E\uDD14Кажется, количество топлива введено неверно
        
        Число не должно быть негативным или нулём
        Введите число литров, например: 2.0 или 40."""),

    /**
     * Некорректная цена за литр топлива (не число).
     */
    INCORRECT_PRICE_NOT_NUMBER(false, """
        ⚠️ Не удалось сохранить стоимость.
        
        Похоже, вы ввели некорректное значение.
        Укажите цену за литр числом в BYN — например: 2.75.
        
        \uD83D\uDC47 Попробуйте ещё раз:"""),

    /**
     * Некорректная цена за литр топлива (число равно 0 или негативное).
     */
    INCORRECT_PRICE_NEGATIVE(false, """
        ⚠️ Не удалось сохранить стоимость
        
        Похоже, вы ввели некорректное значение.
        Число не должно быть негативным или нулём.
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
