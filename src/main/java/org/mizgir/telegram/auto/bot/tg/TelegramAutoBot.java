package org.mizgir.telegram.auto.bot.tg;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mizgir.telegram.auto.bot.configuration.property.TelegramBotConfigProperties;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.model.DataResult;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.model.KeyboardData;
import org.mizgir.telegram.auto.bot.model.KeyboardData.KeyboardType;
import org.mizgir.telegram.auto.bot.service.AutoRegistrationService;
import org.mizgir.telegram.auto.bot.service.ExpensesService;
import org.mizgir.telegram.auto.bot.service.InfoService;
import org.mizgir.telegram.auto.bot.service.MaintenanceService;
import org.mizgir.telegram.auto.bot.service.RefuelService;
import org.mizgir.telegram.auto.bot.service.UserActionStateService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import tools.jackson.databind.ObjectMapper;

/**
 * Telegram bot - паспорт автомобиля.
 *
 * @author Ирина Мизгир
 * @date 03.10.2026 18:28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramAutoBot extends DefaultLongPollingUpdateConsumer implements SpringLongPollingBot {

  private final TelegramBotConfigProperties configProperties;

  private final TelegramClient telegramClient;

  private final ObjectMapper objectMapper;

  private final AutoRegistrationService autoRegistrationService;

  private final UserActionStateService userActionStateService;

  private final RefuelService refuelService;

  private final MaintenanceService maintenanceService;

  private final ExpensesService expensesService;

  private final InfoService infoService;

  @Override
  public void consume(Update update) {
    log.info("onUpdateReceived");
    if (update.hasMessage() && update.getMessage().hasText()) {
      String text = update.getMessage().getText();
      if (StringUtils.isNotBlank(text)) {
        if (Command.isCommand(text)) {
          Command command = Command.fromCommandName(StringUtils.substringAfter(text, Command.COMMAND_PREFIX));
          switch (command) {
            case START -> onStart(update);
            case REFUEL -> onRefuel(update);
            case SERVICE -> onMaintenance(update);
            case COSTS -> onCosts(update);
            case INFO -> onInfo(update);
          }
        } else {
          Long id = update.getMessage().getFrom().getId();
          ActionType type = userActionStateService.findActionTypeById(id);
          switch (type) {
            case REGISTER_AUTO -> onRegisterAutoMessage(id, text, update);
            case ENTER_REFUEL -> onEnterRefuelAutoMassage(id, text, update);
            case ENTER_MAINTENANCE -> onEnterMaintenanceAutoMassage(id, text, update);
            case ENTER_EXPENSES -> onEnterExpensesAutoMassage(id, text, update);
            case ENTER_INFO_BY_PERIOD -> onEnterInfoByPeriodAutoMassage(id, text, update);
            case ENTER_INFO_BY_YEAR -> onEnterInfoByYearAutoMassage(id, text, update);
            case ENTER_INFO_BY_MONTH -> onEnterInfoByMonthAutoMassage(id, text, update);
            case ENTER_INFO_BY_DAY -> onEnterInfoByDayAutoMassage(id, text, update);
          }

          //TODO Обработка ввода данных.
        }

      }
    } else if (update.hasCallbackQuery()) {
      CallbackQuery callbackQuery = update.getCallbackQuery();
      String data = callbackQuery.getData();
      if (StringUtils.isNotEmpty(data)) {
        KeyboardData keyboardData = objectMapper.readValue(data, KeyboardData.class);
        if (keyboardData != null) {

          Long userId = callbackQuery.getFrom().getId();
          if (KeyboardType.REGISTER.equals(keyboardData.keyboardType())) {
            String userName = callbackQuery.getFrom().getUserName();
            autoRegistrationService.create(userId, userName);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("\uD83D\uDE97 Добавим автомобиль\n"
                    + "\nВведите марку и модель одним сообщением.\n"
                    + "Например: Toyota Camry, Volkswagen Passat, Lada Vesta.\n"
                    + "\n\uD83D\uDC47 Напишите марку и модель:")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_PERIOD.equals(keyboardData.keyboardType())) {
            infoService.createByPeriod(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("\uD83D\uDCC5 С какого дня показать статистику?\n"
                    + "\nВведите дату начала периода в формате ДД.ММ.ГГГГ.\n"
                    + "Например: 01.10.2026.\n"
                    + "\n"
                    + "\uD83D\uDC47Ждём дату:")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_YEAR.equals(keyboardData.keyboardType())) {
            infoService.createByYear(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("\uD83D\uDCC5 За какой год показать статистику?\n"
                    + "\nВведите год в формате ГГГГ.\n"
                    + "Например: 2026.\n"
                    + "\n"
                    + "\uD83D\uDC47Ждём год:")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_MONTH.equals(keyboardData.keyboardType())) {
            infoService.createByMonth(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("За какой месяц показать статистику?\n"
                    + "\nВведите месяц числом от 1 до 12.\n"
                    + "Например: 6 — это июнь.\n"
                    + "\n"
                    + "\uD83D\uDC47Ждём месяц:")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_DAY.equals(keyboardData.keyboardType())) {
            infoService.createByDay(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("\uD83D\uDCC5 За какой день показать статистику?\n"
                    + "\nВведите дату в формате ДД.ММ.ГГГГ.\n"
                    + "Например: 01.10.2026.\n"
                    + "\n"
                    + "\uD83D\uDC47Ждём дату:")
                .build();
            sendTelegramMessage(sendMessage);
          }
        }
      }
    }
  }


  /**
   * Обработчик для внесенных данных регистрации.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных регистрации.
   */
  private void onRegisterAutoMessage(Long telegramUserId, String text, Update update) {
    EnterResult result = autoRegistrationService.enterAutoData(telegramUserId, text);

    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .build();

    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных заправки.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных заправки.
   */
  private void onEnterRefuelAutoMassage(Long telegramUserId, String text, Update update) {
    EnterResult result = refuelService.enterRefuelData(telegramUserId, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных СО.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных СО.
   */
  private void onEnterMaintenanceAutoMassage(Long telegramUserId, String text, Update update) {
    EnterResult result = maintenanceService.enterMaintenanceData(telegramUserId, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных расхода.
   *
   * @param telegramUserId Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных расхода.
   */
  private void onEnterExpensesAutoMassage(Long telegramUserId, String text, Update update) {
    EnterResult result = expensesService.enterExpensesData(telegramUserId, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных статистики за период.
   *
   * @param id Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных статистики за период.
   */
  private void onEnterInfoByPeriodAutoMassage(Long id, String text, Update update) {
    EnterResult result = infoService.enterInfoReportDataByPeriod(id, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .replyMarkup(createKeyBoard(result.types()))
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных статистики за год.
   *
   * @param id Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных статистики за год.
   */
  private void onEnterInfoByYearAutoMassage(Long id, String text, Update update) {
    EnterResult result = infoService.enterInfoReportDataByYear(id, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .replyMarkup(createKeyBoard(result.types()))
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных статистики за месяц.
   *
   * @param id Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных статистики за месяц.
   */
  private void onEnterInfoByMonthAutoMassage(Long id, String text, Update update) {
    EnterResult result = infoService.enterInfoReportDataByMonth(id, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .replyMarkup(createKeyBoard(result.types()))
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесенных данных статистики за день.
   *
   * @param id Идентификатор пользователя.
   * @param text Сообщение пользователя.
   * @param update Обновление данных статистики за месяц.
   */
  private void onEnterInfoByDayAutoMassage(Long id, String text, Update update) {
    EnterResult result = infoService.enterInfoReportDataByDay(id, text);
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(result.message())
        .replyMarkup(createKeyBoard(result.types()))
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для запуска бота.
   *
   * @param update Обновление чатов бота.
   */
  private void onStart(Update update) {
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(
            "Привет! Я – ваш цифровой паспорт автомобиля. Помогу держать всю важную информацию о машине в одном месте и вовремя напомню о главном. Что я умею: "
                + "\n📂 Хранить данные авто."
                + "\n📅 Напоминать об окончании срока страховки и техосмотра. "
                + "\n🛠 Вести электронную сервисную книжку (историю ТО и замен). "
                + "\n💰 Считать расходы на бензин и ремонт. "
                + "\nЧтобы начать работу и создать профиль автомобиля, нажмите кнопку ниже:")

        .replyMarkup(createKeyBoard(KeyboardType.REGISTER))
        .build();

    sendTelegramMessage(sendMessage);

  }

  private InlineKeyboardMarkup createKeyBoard(KeyboardType keyboardType) {
    return InlineKeyboardMarkup.builder()
        .keyboardRow(new InlineKeyboardRow(createKeyboardButton(keyboardType))).build();
  }

  private InlineKeyboardButton createKeyboardButton(KeyboardType keyboardType) {
    return InlineKeyboardButton.builder()
        .text(keyboardType.getMessage())
        .callbackData(createRegisterCallbackData(keyboardType))
        .build();
  }

  /**
   * Создание данных клавиатуры бота.
   *
   * @return Данные с клавиатуры бота.
   */
  private String createRegisterCallbackData(KeyboardType keyboardType) {
    return objectMapper.writeValueAsString(new KeyboardData(keyboardType, null));
  }

  private InlineKeyboardMarkup createKeyBoard(Set<KeyboardType> keyboardTypes) {
    List<InlineKeyboardButton> buttons = keyboardTypes.stream()
        .map(this::createKeyboardButton)
        .toList();

    return InlineKeyboardMarkup.builder()
        .keyboardRow(new InlineKeyboardRow(buttons))
        .build();
  }

  private void sendTelegramMessage(SendMessage sendMessage) {
    try {
      telegramClient.execute(sendMessage);
    } catch (TelegramApiException e) {
      log.error("Error on send message", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Обработчик для внесения данных заправки.
   *
   * @param update Обновление чатов бота.
   */
  private void onRefuel(Update update) {

    boolean result = refuelService.create(update.getMessage().getFrom().getId());
    if (result) {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("⛽\uFE0F Заправка автомобиля\n"
              + "\n"
              + "Введите текущий пробег автомобиля в километрах.\n"
              + "Например: 120500 или 608.\n"
              + "\n"
              + "\uD83D\uDC47 Напишите пробег:")
          .build();

      sendTelegramMessage(sendMessage);
    } else {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("\uD83D\uDE97 Автомобиль не найден "
              + "\nПохоже, вы ещё не добавили свой автомобиль в гараж."
              + "\nДавайте это исправим — это займёт меньше минуты!"
              + "\n\uD83D\uDC47 Нажмите кнопку ниже, чтобы добавить автомобиль: ")
          .build();

      sendTelegramMessage(sendMessage);
    }
  }

  /**
   * Обработчик для внесения данных CO.
   *
   * @param update Обновление чатов бота.
   */
  private void onMaintenance(Update update) {
    boolean result = maintenanceService.create(update.getMessage().getFrom().getId());
    if (result) {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("\uD83D\uDD27 Сервисное обслуживание\n"
              + "\n"
              + "Введите текущий пробег автомобиля в километрах.\n"
              + "Например: 608 или 120500.\n"
              + "\n"
              + "\uD83D\uDC47Напишите пробег:")
          .build();
      sendTelegramMessage(sendMessage);
    } else {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("\uD83D\uDE97 Автомобиль не найден "
              + "\nПохоже, вы ещё не добавили свой автомобиль в гараж."
              + "\nДавайте это исправим — это займёт меньше минуты!"
              + "\n\uD83D\uDC47 Нажмите кнопку ниже, чтобы добавить автомобиль: ")
          .build();

      sendTelegramMessage(sendMessage);
    }
  }

  /**
   * Обработчик для внесения данных о расходах.
   *
   * @param update Обновление чатов бота.
   */
  private void onCosts(Update update) {
    boolean result = expensesService.create(update.getMessage().getFrom().getId());
    if (result) {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("\uD83D\uDCB8 Добавление расхода\n"
              + "\n"
              + "Опишите, на что вы потратились.\n"
              + "Например: «Куплен антифриз», «Замена масла», «Мойка автомобиля».\n"
              + "\n"
              + "✍️ Введите описание расхода:")
          .build();
      sendTelegramMessage(sendMessage);
    } else {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("""
              \uD83D\uDE97 Автомобиль не найден\s
              Похоже, вы ещё не добавили свой автомобиль в гараж.
              Давайте это исправим — это займёт меньше минуты!
              \uD83D\uDC47 Нажмите кнопку ниже, чтобы добавить автомобиль:\s""")
          .build();

      sendTelegramMessage(sendMessage);
    }
  }

  /**
   * Обработчик для внесения данных статистики.
   *
   * @param update Обновление чатов бота.
   */
  private void onInfo(Update update) {
    DataResult dataResult = infoService.getDataResult(update.getMessage().getFrom().getId());
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(dataResult.message())
        .replyMarkup(createKeyBoard(dataResult.types()))
        .build();
    sendTelegramMessage(sendMessage);
  }


  @Override
  public String getBotToken() {
    return configProperties.token();
  }

  @Override
  public LongPollingUpdateConsumer getUpdatesConsumer() {
    return this;
  }

  /**
   * Команды для бота.
   */
  private enum Command {

    /**
     * Начало диалога бота.
     */
    START,

    /**
     * Заправка автомобиля.
     */
    REFUEL,

    /**
     * Сервисное обслуживание автомобиля.
     */
    SERVICE,

    /**
     * Расходы на автомобиль.
     */
    COSTS,

    /**
     * Статистика.
     */
    INFO;

    private static final String COMMAND_PREFIX = "/";

    /**
     * Проверка, что сообщения является командой.
     *
     * @param message Сообщение.
     * @return Результат проверки.
     */
    private static boolean isCommand(String message) {
      if (Strings.CS.startsWith(message, COMMAND_PREFIX)) {
        String commandName = StringUtils.substringAfter(message, COMMAND_PREFIX);
        return Stream.of(values())
            .map(Enum::name)
            .anyMatch(commandName::equalsIgnoreCase);
      }
      return false;
    }

    /**
     * Получение объекта команды по имени команды.
     *
     * @param commandName Имя команды.
     * @return Объект команды.
     */
    private static Command fromCommandName(String commandName) {
      return Stream.of(values())
          .filter(command -> command.name().equalsIgnoreCase(commandName))
          .findFirst()
          .orElseThrow(() -> new UnknownCommandException("Неизвестная команда: " + commandName));
    }

  }

  /**
   * Исключение при неизвестной команде.
   */
  private static class UnknownCommandException extends RuntimeException {

    public UnknownCommandException(String message) {
      super(message);
    }
  }
}
