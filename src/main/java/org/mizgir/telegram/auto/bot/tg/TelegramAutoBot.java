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
import org.mizgir.telegram.auto.bot.service.AutoService;
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

  private final AutoService autoService;

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
            autoService.create(userId, userName);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("""
                    \uD83D\uDE97 Добавим автомобиль
                    
                    Введите марку и модель одним сообщением.
                    Например: Toyota Camry, Volkswagen Passat, Lada Vesta.
                    
                    \uD83D\uDC47 Напишите марку и модель:""")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_PERIOD.equals(keyboardData.keyboardType())) {
            infoService.createByPeriod(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("""
                    \uD83D\uDCC5 С какого дня показать статистику?
                    
                    Введите дату начала периода в формате ДД.ММ.ГГГГ.
                    Например: 01.10.2026.
                    
                    \uD83D\uDC47Ждём дату:""")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_YEAR.equals(keyboardData.keyboardType())) {
            infoService.createByYear(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("""
                    \uD83D\uDCC5 За какой год показать статистику?
                    
                    Введите год в формате ГГГГ.
                    Например: 2026.
                    
                    \uD83D\uDC47Ждём год:""")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_MONTH.equals(keyboardData.keyboardType())) {
            infoService.createByMonth(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("""
                    За какой месяц показать статистику?
                    
                    Введите месяц числом от 1 до 12.
                    Например: 6 — это июнь.
                    
                    \uD83D\uDC47Ждём месяц:""")
                .build();
            sendTelegramMessage(sendMessage);
          } else if (KeyboardType.INFO_BY_DAY.equals(keyboardData.keyboardType())) {
            infoService.createByDay(userId);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("""
                    \uD83D\uDCC5 За какой день показать статистику?
                    
                    Введите дату в формате ДД.ММ.ГГГГ.
                    Например: 01.10.2026.
                    
                    \uD83D\uDC47Ждём дату:""")
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
    EnterResult result = autoService.enterAutoData(telegramUserId, text);

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
            """
                Привет! Я – ваш цифровой паспорт автомобиля. Помогу держать всю важную информацию о машине в одном месте и вовремя напомню о главном. Что я умею:
                📂 Хранить данные авто.
                📅 Напоминать об окончании срока страховки и техосмотра.
                🛠 Вести электронную сервисную книжку (историю ТО и замен).
                💰 Считать расходы на бензин и ремонт.
                Чтобы начать работу и создать профиль автомобиля, нажмите кнопку ниже:""")

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

    sendDataResultMessage(update, refuelService.create(update.getMessage().getFrom().getId()));

  }

  /**
   * Обработчик для внесения данных CO.
   *
   * @param update Обновление чатов бота.
   */
  private void onMaintenance(Update update) {
    sendDataResultMessage(update, maintenanceService.create(update.getMessage().getFrom().getId()));
  }

  /**
   * Обработчик для внесения данных о расходах.
   *
   * @param update Обновление чатов бота.
   */
  private void onCosts(Update update) {
    sendDataResultMessage(update, expensesService.create(update.getMessage().getFrom().getId()));
  }

  /**
   * Отправит сообщение по данным.
   *
   * @param update Обновление чат бота.
   * @param dataResult объект результата данных.
   */
  private void sendDataResultMessage(Update update, DataResult dataResult) {
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(dataResult.message())
        .replyMarkup(createKeyBoard(dataResult.types()))
        .build();
    sendTelegramMessage(sendMessage);
  }

  /**
   * Обработчик для внесения данных статистики.
   *
   * @param update Обновление чатов бота.
   */
  private void onInfo(Update update) {
    sendDataResultMessage(update, infoService.getDataResult(update.getMessage().getFrom().getId()));
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
