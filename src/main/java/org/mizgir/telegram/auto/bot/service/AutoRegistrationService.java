package org.mizgir.telegram.auto.bot.service;

import java.time.Year;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mizgir.telegram.auto.bot.entity.AutoEntity;
import org.mizgir.telegram.auto.bot.entity.AutoRegistrationEntity;
import org.mizgir.telegram.auto.bot.entity.AutoRegistrationEntity.RegistrationStatus;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.entity.UserEntity;
import org.mizgir.telegram.auto.bot.exception.AutoRegistrationNotFoundException;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.repository.AutoRegistrationRepository;
import org.mizgir.telegram.auto.bot.repository.UserActionStateRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с данными регистрации автомобиля.
 *
 * @author Ирина Мизгир
 * @date 07.10.2026 22:33
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoRegistrationService {

  /**
   * Самый старый автомобиль на ходу в мире (Benz Victoria Nr. 99).
   */
  private static final Year MAX_OLD_YEAR = Year.of(1894);

  private final AutoRegistrationRepository autoRegistrationRepository;

  private final UserActionStateRepository userActionStateRepository;

  private final UserService userService;

  private final AutoService autoService;

  /**
   * Создание новой регистрации автомобиля.
   *
   * @param telegramUserId Идентификатор пользователя телеграма.
   */
  public void create(Long telegramUserId, String userName) {
    UserEntity userEntity = userService.create(telegramUserId, userName);
    var autoRegistrationEntity = new AutoRegistrationEntity();
    autoRegistrationEntity.setTelegramUserId(telegramUserId);
    autoRegistrationRepository.save(autoRegistrationEntity);
    var userActionStateEntity = new UserActionStateEntity();
    userActionStateEntity.setId(telegramUserId);
    userActionStateEntity.setUser(userEntity);
    userActionStateEntity.setType(ActionType.REGISTER_AUTO);
    userActionStateRepository.save(userActionStateEntity);
  }

  /**
   * Ввод информации об автомобиле.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text           Введенные данные об автомобиле в виде текста.
   * @return Результат.
   */
  public EnterResult enterAutoData(Long telegramUserId, String text) {
    AutoRegistrationEntity autoRegistration = findAutoRegistrationById(telegramUserId);
    AutoEntity autoEntity = Objects.requireNonNullElseGet(
        autoRegistration.getAuto(),
        () -> autoService.newAuto(telegramUserId)
    );
    RegistrationStatus status = autoRegistration.getStatus();
    EnterInformationResultType resultType = switch (status) {
      case ENTER_NAME -> {
        autoEntity.setName(text);
        yield EnterInformationResultType.SUCCESS_ENTER_NAME;
      }
      case ENTER_YEAR -> {

        if (NumberUtils.isDigits(text)) {
          int yearAsInt = Integer.parseInt(text);
          if (yearAsInt >= MAX_OLD_YEAR.getValue() && yearAsInt <= Year.now().getValue()) {
            autoEntity.setYear(Year.of(yearAsInt));
            yield EnterInformationResultType.SUCCESS_ENTER_YEAR;
          }
        }
        yield EnterInformationResultType.INCORRECT_YEAR;
      }
      case ENTER_RUN -> {
        if (NumberUtils.isDigits(text)) {
          autoEntity.setRun(Long.parseLong(text));
          yield EnterInformationResultType.SUCCESS;
        }
        yield EnterInformationResultType.INCORRECT_RUN;
      }
      default -> throw new RuntimeException("Incorrect auto registration status");
    };

    if (resultType.success) {
      autoRegistration.setAuto(autoEntity);
      RegistrationStatus nextStatus = RegistrationStatus.getNextStatus(status);
      autoRegistration.setStatus(nextStatus);
      autoRegistrationRepository.save(autoRegistration);

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

  /**
   * Найдет объект регистрации автомобиля по идентификатору.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @return Объект регистрации автомобиля.
   */
  private AutoRegistrationEntity findAutoRegistrationById(long telegramUserId) {
    return autoRegistrationRepository.findById(telegramUserId)
        .orElseThrow(() -> new AutoRegistrationNotFoundException(telegramUserId));
  }

  /**
   * Тип результата ввода информации.
   */
  @RequiredArgsConstructor
  public enum EnterInformationResultType {

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
    SUCCESS_ENTER_YEAR(true, "\uD83D\uDEE3 Введите текущий пробег\n"
        + "\nУкажите пробег в километрах, только число.\n"
        + "\n\uD83D\uDC47 Напишите пробег:"),

    /**
     * Некорректный год.
     */
    INCORRECT_YEAR(false, "⚠️ Не удалось сохранить год выпуска\n"
        + "\n"
        + "Введите год числом в диапазоне 1894–2026.\n"
        + "Например: 2006.\n"
        + "\n"
        + "\uD83D\uDC47 Попробуйте ещё раз:"),

    /**
     * Некорректный пробег.
     */
    INCORRECT_RUN(false, "⚠️ Неверный пробег\n"
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
