package org.mizgir.telegram.auto.bot.tg;

import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mizgir.telegram.auto.bot.configuration.property.TelegramBotConfigProperties;
import org.mizgir.telegram.auto.bot.entity.UserActionStateEntity.ActionType;
import org.mizgir.telegram.auto.bot.model.EnterResult;
import org.mizgir.telegram.auto.bot.model.KeyboardData;
import org.mizgir.telegram.auto.bot.model.KeyboardData.Type;
import org.mizgir.telegram.auto.bot.service.AutoRegistrationService;
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
          }
        } else {
          Long id = update.getMessage().getFrom().getId();
          ActionType type = userActionStateService.findActionTypeById(id);
          switch (type) {
            case REGISTER_AUTO -> onRegisterAutoMessage(id, text, update);
            case ENTER_REFUEL -> onEnterRefuelAutoMassage(id, text, update);
            case ENTER_MAINTENANCE -> onEnterMaintenanceAutoMassage(id, text, update);
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

          if (Type.REGISTER.equals(keyboardData.type())) {
            Long userId = callbackQuery.getFrom().getId();
            String userName = callbackQuery.getFrom().getUserName();
            autoRegistrationService.create(userId, userName);
            SendMessage sendMessage = SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("Давай добавим твой автомобиль. Введи марку и модель:")
                .build();
            sendTelegramMessage(sendMessage);

//TODO Написать обработку регистрации.
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
   * @param update Обновление данных заправки.
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
   * Обработчик для запуска бота.
   *
   * @param update Обновление чатов бота.
   */
  private void onStart(Update update) {
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text(
            "Привет! Я – твой цифровой паспорт автомобиля. Помогу держать всю важную информацию о машине в одном месте и вовремя напомню о главном. Что я умею: "
                + "📂 Хранить данные авто."
                + "📅 Напоминать об окончании срока страховки и техосмотра "
                + "🛠 Вести электронную сервисную книжку (историю ТО и замен) "
                + "💰 Считать расходы на бензин и ремонт "
                + "Чтобы начать работу и создать профиль автомобиля, нажмите кнопку ниже:")

        .replyMarkup(
            InlineKeyboardMarkup.builder()
                .keyboardRow(
                    new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                            .text("🚗 Создать профиль автомобиля")
                            .callbackData(createRegisterCallbackData())
                            .build()
                    )
                ).build()
        ).build();

    sendTelegramMessage(sendMessage);

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
          .text("Заправка автомобиля.Введи пробег: ")
          .build();

      sendTelegramMessage(sendMessage);
    } else {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("Автомобиль не найден. Добавь, пожалуйста, автомобиль: ")
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
    boolean resul = maintenanceService.create(update.getMessage().getFrom().getId());
    if (resul) {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("Сервисное обслуживание автомабиля. Введите пробег:")
          .build();
      sendTelegramMessage(sendMessage);
    } else {
      SendMessage sendMessage = SendMessage.builder()
          .chatId(update.getMessage().getChatId())
          .text("Автомобиль не найден. Добавь, пожалуйста, автомобиль: ")
          .build();

      sendTelegramMessage(sendMessage);
    }
  }

  /**
   * Создание данных клавиатуры бота.
   *
   * @return Данные с клавиатуры бота.
   */
  private String createRegisterCallbackData() {
    return objectMapper.writeValueAsString(new KeyboardData(Type.REGISTER, null));
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
     * Сервисное обслуживание автомобиля
     */
    SERVICE;

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
