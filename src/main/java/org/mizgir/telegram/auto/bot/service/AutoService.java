package org.mizgir.telegram.auto.bot.service;

import java.time.Year;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.AutoEntity.RegistrationStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.entity.UserEntity;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.repository.AutoRepository;
import org.mizgir.telegram.auto.bot.util.NumberOps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с автомобилями.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 16:18
 */
@Service
@RequiredArgsConstructor
public class AutoService {

  /**
   * Самый старый автомобиль на ходу в мире (Benz Victoria Nr. 99).
   */
  private static final Year MAX_OLD_YEAR = Year.of(1894);

  private final AutoRepository autoRepository;

  private final UserService userService;

  private final UserActionStateService userActionStateService;

  /**
   * Создание новой регистрации автомобиля.
   *
   * @param telegramUserId Идентификатор пользователя телеграма.
   */
  @Transactional
  public void create(Long telegramUserId, String userName) {
    UserEntity userEntity = userService.upsert(telegramUserId, userName);
    AutoEntity autoEntity = autoRepository.findByUserId(telegramUserId).orElseGet(() -> {
      var newAutoEntity = new AutoEntity();
      newAutoEntity.setUser(userEntity);

      return newAutoEntity;
    });
    autoEntity.setStatus(RegistrationStatus.ENTER_NAME);
    autoRepository.save(autoEntity);
    userActionStateService.upsert(telegramUserId, userEntity, ActionType.REGISTER_AUTO);
  }

  /**
   * Ввод информации об автомобиле.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данные об автомобиле в виде текста.
   * @return Результат.
   */
  public EnterResult enterAutoData(Long telegramUserId, String text) {
    AutoEntity autoEntity = getByUserId(telegramUserId);
    RegistrationStatus status = autoEntity.getStatus();
    EnterInformationResultType resultType = trySetNewData(text, status, autoEntity);

    if (resultType.success) {
      RegistrationStatus nextStatus = RegistrationStatus.getNextStatus(status);
      autoEntity.setStatus(nextStatus);
      autoRepository.save(autoEntity);

      if (EnterInformationResultType.SUCCESS.equals(resultType)) {
        var autoInfoStringBuilder = new StringBuilder();
        if (StringUtils.isNoneEmpty(autoEntity.getName())) {
          autoInfoStringBuilder
              .append("\n\uD83D\uDE98 Марка и модель: ")
              .append(autoEntity.getName())
              .append(". ");
        }
        if (autoEntity.getYear() != null) {
          autoInfoStringBuilder
              .append("\n\uD83D\uDCC5Год выпуска: ")
              .append(autoEntity.getYear())
              .append(". ");

        }
        if (autoEntity.getRun() != null) {
          autoInfoStringBuilder
              .append("\n\uD83D\uDEE3 Пробег: ")
              .append(autoEntity.getRun())
              .append(" км.");
        }

        return new EnterResult(true, resultType.message + autoInfoStringBuilder);
      }
    }
    return new EnterResult(resultType.success, resultType.message);
  }

  private @NonNull EnterInformationResultType trySetNewData(
      String text,
      RegistrationStatus status,
      AutoEntity autoEntity
  ) {
    return switch (status) {
      case ENTER_NAME -> {
        autoEntity.setName(text);
        yield EnterInformationResultType.SUCCESS_ENTER_NAME;
      }
      case ENTER_YEAR -> {
        if (NumberOps.isNotDigits(text)) {
          yield EnterInformationResultType.INCORRECT_YEAR_NOT_NUMBER;
        }
        int yearAsInt = Integer.parseInt(text);
        if (yearAsInt < MAX_OLD_YEAR.getValue()) {
          yield EnterInformationResultType.INCORRECT_YEAR_TOO_OLD;
        }
        if (yearAsInt > Year.now().getValue()) {
          yield EnterInformationResultType.INCORRECT_YEAR_FUTURE;
        }

        autoEntity.setYear(Year.of(yearAsInt));
        yield EnterInformationResultType.SUCCESS_ENTER_YEAR;
      }
      case ENTER_RUN -> {
        if (NumberOps.isNotDigits(text)) {
          yield EnterInformationResultType.INCORRECT_RUN_NOT_NUMBER;
        }
        autoEntity.setRun(Long.parseLong(text));
        yield EnterInformationResultType.SUCCESS;
      }
      default -> throw new RuntimeException("Incorrect auto registration status");
    };
  }

  /**
   * Получение автомобиля по идентификатору пользователя.
   *
   * @param userId Идентификатор пользователя.
   * @return Автомобиль.
   */
  public AutoEntity getByUserId(long userId) {
    return autoRepository.getByUserId(userId);
  }

  /**
   * Получение автомобиля по идентификатору пользователя.
   *
   * @param userId Идентификатор пользователя.
   * @return Автомобиль.
   */
  public Optional<AutoEntity> findByUserIdAndFinished(long userId) {
    return autoRepository.findByUserIdAndStatus(userId, RegistrationStatus.FINISHED);
  }

  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  private enum EnterInformationResultType {

    /**
     * Успешно введено имя.
     */
    SUCCESS_ENTER_NAME(true, """
        \uD83D\uDCC5 Введите год выпуска
        
         год числом, например: 2006.
        
        \uD83D\uDC47 Напишите год:"""),

    /**
     * Успешно введен год.
     */
    SUCCESS_ENTER_YEAR(true, """
        \uD83D\uDEE3 Введите текущий пробег
        
        Укажите пробег в километрах, только число.
        
        \uD83D\uDC47 Напишите пробег:"""),

    /**
     * Некорректный год(не число).
     */
    INCORRECT_YEAR_NOT_NUMBER(false, """
        ⚠️ Не удалось сохранить год выпуска
        
        Не является числом
        Введите год числом в диапазоне 1894–2026.
        Например: 2006.
        
        \uD83D\uDC47 Попробуйте ещё раз:"""),

    /**
     * Некорректный год(слишком старый).
     */
    INCORRECT_YEAR_TOO_OLD(false, """
        ⚠️ Не удалось сохранить год выпуска
        
        Слишком старый
        Введите год числом в диапазоне 1894–2026.
        Например: 2006.
        
        \uD83D\uDC47 Попробуйте ещё раз:"""),

    /**
     * Некорректный год(больше текущего года).
     */
    INCORRECT_YEAR_FUTURE(false, """
        ⚠️ Не удалось сохранить год выпуска
        
        больше текущего года
        Введите год числом в диапазоне 1894–2026.
        Например: 2006.
        
        \uD83D\uDC47 Попробуйте ещё раз:"""),

    /**
     * Некорректный пробег.
     */
    INCORRECT_RUN_NOT_NUMBER(false, "⚠️ Неверный пробег\n"
        + "Введите число в километрах, например: 120000."),

    /**
     * Успешно.
     */
    SUCCESS(true, "✅ Автомобиль добавлен");

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
