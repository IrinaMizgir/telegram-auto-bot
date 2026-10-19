package org.mizgir.telegram.auto.bot.service;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.NonNull;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.InfoReportEntity;
import org.mizgir.telegram.auto.bot.entity.InfoReportEntity.InfoReportStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.model.DataResult;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.model.KeyboardData.KeyboardType;
import org.mizgir.telegram.auto.bot.model.RefuelSummary;
import org.mizgir.telegram.auto.bot.repository.InfoReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы со статистикой.
 * @author Ирина Мизгир
 * @date 17.10.2026 14:38
 */
@Service
@RequiredArgsConstructor
public class InfoService {

  private final AutoService autoService;

  private final RefuelService refuelService;

  private final MaintenanceService maintenanceService;

  private final ExpensesService expensesService;

  private final InfoReportRepository infoReportRepository;

  private final UserActionStateService userActionStateService;

  /**
   * Создание новой записи отчёта.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат отчёт.
   */
  @Transactional
  public boolean createByPeriod(Long telegramUserId) {
    return createByActionType(telegramUserId, ActionType.ENTER_INFO_BY_PERIOD);
  }

  /**
   * Создание новой записи отчёта.
   * @param telegramUserId Идентификатор пользователя.
   * @param actionType Тип действия.
   * @return true - если начат отчёт.
   */
  private boolean createByActionType(Long telegramUserId, ActionType actionType) {
    Optional<AutoEntity> autoEntity = autoService.findByUserId(telegramUserId);
    if (autoEntity.isEmpty()) {
      return false;
    }
    userActionStateService.changeActionTypeById(telegramUserId, actionType);
    autoEntity.ifPresent(this::upsertInfoReport);

    return true;
  }

  /**
   * Создание записи отчёта для указанного автомобиля.
   *
   * @param autoEntity Автомобиль, для которого создаётся запись отчёта.
   */
  private void upsertInfoReport(AutoEntity autoEntity) {
    infoReportRepository.findByAutoIdAndStatusNot(autoEntity.getId(), InfoReportStatus.FINISHED)
        .ifPresentOrElse(
            infoReportEntity -> onUpsertOnFindInfoReport(infoReportEntity, autoEntity),
            () -> create(autoEntity)
        );
  }

  /***
   * Обрабатывает найденную существующую запись отчёта.
   *
   * @param infoReportEntity Найденная существующий отчёт.
   * @param autoEntity Автомобиль, для которого создаётся запись отчёта.
   */
  private void onUpsertOnFindInfoReport(InfoReportEntity infoReportEntity, AutoEntity autoEntity) {
    if (!InfoReportStatus.ENTER_START_DATE.equals(infoReportEntity.getStatus())) {
      infoReportRepository.delete(infoReportEntity);
      create(autoEntity);
    }
  }

  /**
   * Создаёт новую запись о заправке для указанного автомобиля и сохраняет её в репозитории.
   * @param autoEntity Автомобиль, для которого создаётся запись о заправке.
   */
  private void create(AutoEntity autoEntity) {
    InfoReportEntity infoReportEntity = new InfoReportEntity();
    infoReportEntity.setAuto(autoEntity);
    infoReportRepository.save(infoReportEntity);
  }


  /**
   * Создание новой записи отчёта за год.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат отчёт.
   */
  @Transactional
  public boolean createByYear(Long telegramUserId) {
    return createByActionType(telegramUserId, ActionType.ENTER_INFO_BY_YEAR);
  }

  /**
   * Создание новой записи отчёта за месяц.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат отчёт.
   */
  @Transactional
  public boolean createByMonth(Long telegramUserId) {
    return createByActionType(telegramUserId, ActionType.ENTER_INFO_BY_MONTH);
  }

  /**
   * Создание новой записи отчёта за день.
   * @param telegramUserId Идентификатор пользователя.
   * @return true - если начат отчёт.
   */
  @Transactional
  public boolean createByDay(Long telegramUserId) {
    return createByActionType(telegramUserId, ActionType.ENTER_INFO_BY_DAY);
  }

  /**
   * Ввод информации отчёта за год.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данных отчёта за год в виде текста.
   * @return Результат.
   */
  public EnterResult enterInfoReportDataByYear(Long telegramUserId, String text) {
    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    InfoReportEntity infoReportEntity = infoReportRepository.getByAutoIdAndStatusNot(autoEntity.getId(),
        InfoReportStatus.FINISHED);

    if (NumberUtils.isDigits(text)) {
      Year year = Year.of(Integer.parseInt(text));
      LocalDate periodStart = year.atDay(1);
      LocalDate periodEnd = year.atDay(year.length());
      infoReportEntity.setAuto(autoEntity);
      infoReportEntity.setPeriodStartDate(periodStart);
      infoReportEntity.setPeriodEndDate(periodEnd);
      infoReportEntity.setStatus(InfoReportStatus.FINISHED);
      infoReportRepository.save(infoReportEntity);

      return toSuccessEnterResult(autoEntity, infoReportEntity);
    }

    return new EnterResult(false, "🙂 Кажется, год не распознался\n"
        + "\n"
        + "Введите год в формате ГГГГ, например: 2026.\n"
        + "\n"
        + "👇 Попробуем ещё раз?");
  }

  /**
   * Ввод информации отчёта за месяц.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данных отчёта за месяц в виде текста.
   * @return Результат.
   */
  public EnterResult enterInfoReportDataByMonth(Long telegramUserId, String text) {
    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    InfoReportEntity infoReportEntity = infoReportRepository.getByAutoIdAndStatusNot(autoEntity.getId(),
        InfoReportStatus.FINISHED);

    if (NumberUtils.isDigits(text)) {
      int monthAsInt = Integer.parseInt(text);
      if (monthAsInt >= 1 && monthAsInt <= 12) {
        YearMonth yearMonth = YearMonth.of(Year.now().getValue(), Month.of(monthAsInt));
        LocalDate periodStart = yearMonth.atDay(1);
        LocalDate periodEnd = yearMonth.atEndOfMonth();
        infoReportEntity.setAuto(autoEntity);
        infoReportEntity.setPeriodStartDate(periodStart);
        infoReportEntity.setPeriodEndDate(periodEnd);
        infoReportEntity.setStatus(InfoReportStatus.FINISHED);
        infoReportRepository.save(infoReportEntity);

        return toSuccessEnterResult(autoEntity, infoReportEntity);
      }

    }

    return new EnterResult(false, "🙂 Кажется, месяц не распознался\n"
        + "\n"
        + "Введите месяц числом от 1 до 12, например: 6 — это июнь.\n"
        + "\n"
        + "👇 Попробуем ещё раз?");
  }

  /**
   * Ввод информации отчёта за день.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данных отчёта за день в виде текста.
   * @return Результат.
   */
  public EnterResult enterInfoReportDataByDay(Long telegramUserId, String text) {
    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    InfoReportEntity infoReportEntity = infoReportRepository.getByAutoIdAndStatusNot(autoEntity.getId(),
        InfoReportStatus.FINISHED);

    LocalDate localDate = parseOrNull(text);
    if (localDate != null) {
      infoReportEntity.setAuto(autoEntity);
      infoReportEntity.setPeriodStartDate(localDate);
      infoReportEntity.setPeriodEndDate(localDate);
      infoReportEntity.setStatus(InfoReportStatus.FINISHED);
      infoReportRepository.save(infoReportEntity);

      return toSuccessEnterResult(autoEntity, infoReportEntity);

    }

    return new EnterResult(false, "\uD83D\uDE42 Кажется, дата не распозналась\n"
        + "\n"
        + "Введите дату в формате ДД.ММ.ГГГГ, например: 31.10.2026.\n"
        + "\n"
        + "\uD83D\uDC47 Попробуем ещё раз?");
  }

  /**
   * Ввод информации отчёта за период.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данных отчёта в виде текста.
   * @return Результат.
   */
  public EnterResult enterInfoReportDataByPeriod(Long telegramUserId, String text) {
    AutoEntity autoEntity = autoService.getByUserId(telegramUserId);
    InfoReportEntity infoReportEntity = infoReportRepository.getByAutoIdAndStatusNot(autoEntity.getId(),
        InfoReportStatus.FINISHED);
    InfoReportStatus status = infoReportEntity.getStatus();
    EnterInfoReportInformationResultType resultType = switch (status) {
      case ENTER_START_DATE -> {
        LocalDate localDate = parseOrNull(text);
        if (localDate == null
            || localDate.isAfter(LocalDate.now())
            || localDate.isAfter(autoEntity.getCreationDate())
        ) {
          yield EnterInfoReportInformationResultType.INCORRECT_START_DATE;
        }
        infoReportEntity.setPeriodStartDate(localDate);
        yield EnterInfoReportInformationResultType.SUCCESS_ENTER_START_DATE;
      }
      case ENTER_END_DATE -> {
        LocalDate localDate = parseOrNull(text);
        if (localDate == null
            || localDate.isAfter(LocalDate.now())
            || localDate.isAfter(autoEntity.getCreationDate())
            || localDate.isBefore(infoReportEntity.getPeriodStartDate())
        ) {
          yield EnterInfoReportInformationResultType.INCORRECT_END_DATE;
        }
        infoReportEntity.setPeriodEndDate(localDate);
        yield EnterInfoReportInformationResultType.SUCCESS;
      }
      default -> throw new RuntimeException("Incorrect info report");
    };
    if (resultType.success) {
      infoReportEntity.setAuto(autoEntity);
      InfoReportStatus nextStatus = InfoReportStatus.getNextStatus(status);
      infoReportEntity.setStatus(nextStatus);
      infoReportRepository.save(infoReportEntity);

      if (EnterInfoReportInformationResultType.SUCCESS.equals(resultType)) {
        return toSuccessEnterResult(autoEntity, infoReportEntity);
      }
    }
    return new EnterResult(resultType.success, resultType.message);
  }

  private @NonNull EnterResult toSuccessEnterResult(AutoEntity autoEntity, InfoReportEntity infoReportEntity) {
    RefuelSummary refuelSummary = refuelService.getRefuelSummaryByAutoAndPeriod(autoEntity,
        infoReportEntity.getPeriodStartDate(),
        infoReportEntity.getPeriodEndDate());
    BigDecimal refuelSumByAuto = refuelSummary.getCostsOrZero();
    BigDecimal maintenanceSumByAuto = maintenanceService.getMaintenanceSummaryByAutoAndPeriod(autoEntity,
        infoReportEntity.getPeriodStartDate(),
        infoReportEntity.getPeriodEndDate());
    BigDecimal expensesSumByAuto = expensesService.getExpensesSummaryByAutoAndPeriod(autoEntity,
        infoReportEntity.getPeriodStartDate(),
        infoReportEntity.getPeriodEndDate());
    BigDecimal averageRefuelCost = refuelSummary.calculateAverageRefuelCost();
    BigDecimal sum = refuelSumByAuto.add(maintenanceSumByAuto).add(expensesSumByAuto);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    String message = infoReportEntity.getPeriodStartDate().format(formatter)
        + "–"
        + infoReportEntity.getPeriodEndDate().format(formatter)
        + "\nТекущий пробег: "
        + autoEntity.getRun()
        + " км."
        + "\nСредняя стоимость за км: "
        + averageRefuelCost
        + " BYN."
        + "\nЗатраты топлива: "
        + refuelSumByAuto
        + " BYN."
        + "\nЗатраты на сервисное обслуживание: "
        + maintenanceSumByAuto
        + " BYN."
        + "\nПрочие расходы: "
        + expensesSumByAuto
        + " BYN."
        + "\nВсего: "
        + sum
        + " BYN."
        + "\nВнизу можно выбрать статистику за период: ";

    return new EnterResult(
        true,
        EnterInfoReportInformationResultType.SUCCESS.message + message,
        ImmutableSet.of(
            KeyboardType.INFO_BY_PERIOD,
            KeyboardType.INFO_BY_YEAR,
            KeyboardType.INFO_BY_MONTH,
            KeyboardType.INFO_BY_DAY
        )
    );
  }

  /**
   * Парсит строку с датой в формате ("dd.MM.yyyy").
   * @param text Введенные данные в виде текста.
   * @return Дата.
   */
  private LocalDate parseOrNull(String text) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
      return LocalDate.from(formatter.parse(text));
    } catch (DateTimeParseException exception) {
      return null;
    }
  }

  /**
   * Получение данных.
   * @param userId Идентификатор пользователя.
   * @return Данные.
   */
  public DataResult getDataResult(long userId) {
    return autoService.findByUserId(userId)
        .map(this::calculateStatistic)
        .orElseGet(this::createAutoNotFoundDataResult);

  }

  /**
   * Создание данные если авто не найдено.
   * @return Данные.
   */
  private @NonNull DataResult createAutoNotFoundDataResult() {
    return new DataResult(
        Collections.singleton(KeyboardType.REGISTER),
        "Автомобиль не найден. Пожалуйста, добавьте автомобиль."
    );

  }

  /**
   * Получение данных статистики.
   * @param autoEntity Сущность автомобиля.
   * @return Данные.
   */
  private DataResult calculateStatistic(AutoEntity autoEntity) {
    RefuelSummary refuelSummary = refuelService.getRefuelSummaryByAuto(autoEntity);
    BigDecimal refuelSumByAuto = refuelSummary.getCostsOrZero();
    BigDecimal maintenanceSumByAuto = maintenanceService.getSumByAuto(autoEntity);
    BigDecimal expensesSumByAuto = expensesService.getSumByAuto(autoEntity);
    BigDecimal averageRefuelCost = refuelSummary.calculateAverageRefuelCost();
    BigDecimal sum = refuelSumByAuto.add(maintenanceSumByAuto).add(expensesSumByAuto);
    String message = "\uD83D\uDCCAОбщая статистика по вашему автомобилю"
        + "\n\uD83D\uDEE3 Текущий пробег:  "
        + autoEntity.getRun()
        + " км."
        + "\n⛽ Средний расход: "
        + averageRefuelCost
        + " BYN."
        + "\n\uD83D\uDCB0 Затраты на топливо: "
        + refuelSumByAuto
        + " BYN."
        + "\n\uD83D\uDD27 Сервисное обслуживание:  "
        + maintenanceSumByAuto
        + " BYN."
        + "\n\uD83D\uDCE6 Прочие расходы:  "
        + expensesSumByAuto
        + " BYN."
        + "\n\uD83D\uDCB5 Итого потрачено:  "
        + sum
        + " BYN."
        + "\n\uD83D\uDCC5 Выберите период для просмотра детальной статистики: ";

    return new DataResult(
        ImmutableSet.of(
            KeyboardType.INFO_BY_PERIOD,
            KeyboardType.INFO_BY_YEAR,
            KeyboardType.INFO_BY_MONTH,
            KeyboardType.INFO_BY_DAY
        ),
        message
    );
  }

  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  public enum EnterInfoReportInformationResultType {

    /**
     * Успешно введенная дата начала периода.
     */
    SUCCESS_ENTER_START_DATE(true, """
        \uD83D\uDCC5 Дата окончания периода
        
        Введите дату в формате ДД.ММ.ГГГГ (например: 31.10.2026):"""),

    /**
     * Успешно введенная дата окончания периода.
     */
    SUCCESS(true, "\uD83D\uDCCAСтатистика по вашему автомобилю за период: \n"),

    /**
     * Некорректная дата начала периода.
     */
    INCORRECT_START_DATE(false, "\uD83D\uDE42 Кажется, дата не распозналась\n"
        + "\n"
        + "Введите дату в формате ДД.ММ.ГГГГ, например: 31.10.2026.\n"
        + "\n"
        + "\uD83D\uDC47 Попробуем ещё раз?"),

    /**
     * Некорректная дата окончания периода.
     */
    INCORRECT_END_DATE(false, "\uD83D\uDE42Кажется, дата не распозналась\n"
        + "\n"
        + "Введите дату в формате ДД.ММ.ГГГГ, например: 31.10.2026.\n"
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
