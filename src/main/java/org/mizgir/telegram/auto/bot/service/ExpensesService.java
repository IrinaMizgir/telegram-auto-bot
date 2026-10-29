package org.mizgir.telegram.auto.bot.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.ExpensesEntity;
import org.mizgir.telegram.auto.bot.entity.ExpensesEntity.ExpensesStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.factory.DataResultFactory;
import org.mizgir.telegram.auto.bot.model.DataResult;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.repository.ExpensesRepository;
import org.mizgir.telegram.auto.bot.util.BigDecimalUtils;
import org.mizgir.telegram.auto.bot.util.NumberOps;
import org.mizgir.telegram.auto.bot.util.StringOps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с расходами на автомобиль.
 * @author Ирина Мизгир
 * @date 16.10.2026 21:54
 */
@Service
@RequiredArgsConstructor
public class ExpensesService {

  private final AutoService autoService;

  private final ExpensesRepository expensesRepository;

  private final UserActionStateService userActionStateService;

  private final DataResultFactory dataResultFactory;

  /**
   * Создание новой записи расходов.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат новый процесс расходов.
   */
  @Transactional
  public DataResult create(Long telegramUserId) {
    Optional<AutoEntity> autoEntity = autoService.findByUserIdAndFinished(telegramUserId);
    if (autoEntity.isEmpty()) {
      return dataResultFactory.createInvalid();
    }
    userActionStateService.changeActionTypeById(telegramUserId, ActionType.ENTER_EXPENSES);
    autoEntity.ifPresent(this::upsertExpenses);

    return dataResultFactory.createValid("""
        \uD83D\uDCB8 Добавление расхода
        
        Опишите, на что вы потратились.
        Например: «Куплен антифриз», «Мойка автомобиля».
        
        ✍️ Введите описание расхода:""");
  }

  /**
   * Создание записи о расходах для указанного автомобиля.
   *
   * @param autoEntity Автомобиль, для которого создаётся запись о расходах.
   */
  private void upsertExpenses(AutoEntity autoEntity) {
    expensesRepository.findByAutoIdAndStatusNot(autoEntity.getId(), ExpensesStatus.FINISHED)
        .ifPresentOrElse(
            expensesEntity -> onUpsertOnFindExpenses(expensesEntity, autoEntity),
            () -> create(autoEntity)
        );
  }

  /***
   * Обрабатывает найденную существующую запись о расходах.
   *
   * @param expensesEntity Найденный существующий запись о расходе.
   * @param autoEntity Автомобиль, для которого создаётся запись о расходах.
   */
  private void onUpsertOnFindExpenses(ExpensesEntity expensesEntity, AutoEntity autoEntity) {
    if (!ExpensesStatus.ENTER_DESCRIPTION.equals(expensesEntity.getStatus())) {
      expensesRepository.delete(expensesEntity);
      create(autoEntity);
    }
  }

  /**
   * Создаёт новую запись о расходах для указанного автомобиля и сохраняет её в репозитории.
   * @param autoEntity Автомобиль, для которого создаётся запись о расходах.
   */
  private void create(AutoEntity autoEntity) {
    ExpensesEntity expensesEntity = new ExpensesEntity();
    expensesEntity.setAuto(autoEntity);
    expensesRepository.save(expensesEntity);
  }

  /**
   * Ввод информации расходов.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данные расходов в виде текста.
   * @return Результат.
   */
  public EnterResult enterExpensesData(Long telegramUserId, String text) {
    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    ExpensesEntity expensesEntity = expensesRepository.getByAutoIdAndStatusNot(autoEntity.getId(),
        ExpensesStatus.FINISHED);
    ExpensesStatus status = expensesEntity.getStatus();
    EnterExpensesInformationResultType resultType = trySetNewData(text, status, expensesEntity);
    if (resultType.success) {
      expensesEntity.setAuto(autoEntity);
      ExpensesStatus nextStatus = ExpensesStatus.getNextStatus(status);
      expensesEntity.setStatus(nextStatus);
      expensesRepository.save(expensesEntity);
      if (EnterExpensesInformationResultType.SUCCESS.equals(resultType)) {
        var expensesInfoStringBuilder = new StringBuilder();
        if (expensesEntity.getDescription() != null) {
          expensesInfoStringBuilder
              .append("\nТип расхода: ")
              .append(expensesEntity.getDescription())
              .append(".");
        }
        if (expensesEntity.getCost() != null) {
          expensesInfoStringBuilder
              .append("\nСумма:")
              .append(expensesEntity.getCost())
              .append(" BYN.");
        }
        return new EnterResult(true, resultType.message + expensesInfoStringBuilder);
      }
    }
    return new EnterResult(resultType.success, resultType.message);
  }

  private @NonNull EnterExpensesInformationResultType trySetNewData(String text,
      ExpensesStatus status, ExpensesEntity expensesEntity) {
    return switch (status) {
      case ENTER_DESCRIPTION -> {
        expensesEntity.setDescription(text);
        yield EnterExpensesInformationResultType.SUCCESS_ENTER_DESCRIPTION;
      }
      case ENTER_COST -> {
        String normalizedText = StringOps.replaceCommaToDot(text);
        if (NumberOps.isNotParsable(normalizedText)) {
          yield EnterExpensesInformationResultType.INCORRECT_COST_NOT_NUMBER;
        }

        double doubleCost = Double.parseDouble(normalizedText);
        if (doubleCost <= 0) {
          yield EnterExpensesInformationResultType.INCORRECT_COST_NEGATIVE;
        }

        expensesEntity.setCost(BigDecimalUtils.valueOf(doubleCost));
        yield EnterExpensesInformationResultType.SUCCESS;
      }
      default -> throw new RuntimeException("Incorrect auto registration status");
    };
  }

  /**
   * Получение общей суммы за расходы.
   * @param autoEntity Сущность автомобиля.
   * @return Сумма расхода за расходы.
   */
  public BigDecimal getSumByAuto(AutoEntity autoEntity) {
    BigDecimal bigDecimal = expensesRepository.sumCostsByAuto(autoEntity);
    if (bigDecimal == null) {
      return BigDecimal.ZERO;
    }
    return bigDecimal;
  }

  /**
   * Получение суммарной информации о расходах за период.
   * @param autoEntity Сущность автомобиля.
   * @return Суммарная информация о расходах за период.
   */
  public BigDecimal getExpensesSummaryByAutoAndPeriod(AutoEntity autoEntity, LocalDate startDate, LocalDate endDate) {

    BigDecimal bigDecimal = expensesRepository.sumCostsByAutoBetweenCreationDate(autoEntity, startDate, endDate);
    if (bigDecimal == null) {
      return BigDecimal.ZERO;
    }
    return bigDecimal;
  }

  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  public enum EnterExpensesInformationResultType {
    /**
     * Успешно введена описание расходов.
     */
    SUCCESS_ENTER_DESCRIPTION(true, """
        \uD83D\uDCB0 Введите сумму расхода
        
        Укажите сумму в белорусских рублях (BYN).
        Например: 21.00 или 21
        
        \uD83D\uDC47 Напишите сумму сообщением:"""),

    /**
     * Некорректная стоимость расходов (строка не является числом).
     */
    INCORRECT_COST_NOT_NUMBER(false, """
        \uD83E\uDD14 Кажется, сумма введена неверно
        
        Не является числом
        Введите число в BYN, например: 21.00."""),

    /**
     * Некорректная стоимость расходов (негативное число и ноль).
     */
    INCORRECT_COST_NEGATIVE(false, """
        \uD83E\uDD14 Кажется, сумма введена неверно
        
        Число не должно быть негативным или нулём
        Введите число в BYN, например: 21.00."""),

    /**
     * Успешно.
     */
    SUCCESS(true, "✅ Расход успешно сохранён");

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
