package org.mizgir.telegram.auto.bot.tg;

import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mizgir.telegram.auto.bot.configuration.property.TelegramBotConfigProperties;
import org.mizgir.telegram.auto.bot.model.KeyboardData;
import org.mizgir.telegram.auto.bot.model.KeyboardData.Type;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

  @Override
  public void consume(Update update) {
    log.info("onUpdateReceived");
    if (update.hasMessage() && update.getMessage().hasText()) {
      String text = update.getMessage().getText();
      if (StringUtils.isNotEmpty(text) && Command.isCommand(text)) {
        Command command = Command.fromCommandName(StringUtils.substringAfter(text, Command.COMMAND_PREFIX));
        switch (command) {
          case START -> onStart(update);
        }
      }
    } else if (update.hasCallbackQuery()) {
      String data = update.getCallbackQuery().getData();
      if (StringUtils.isNotEmpty(data)) {
        KeyboardData keyboardData = objectMapper.readValue(data, KeyboardData.class);
        if (keyboardData != null) {
          //TODO Написать обработку регистрации.
        }
      }
    }


  }

  /**
   * Обработчик для запуска бота.
   *
   * @param update Обновление чатов бота.
   */
  private void onStart(Update update) {
    SendMessage sendMessage = SendMessage.builder()
        .chatId(update.getMessage().getChatId())
        .text("Привет я паспорт твоего авто.")
        .replyMarkup(
            InlineKeyboardMarkup.builder()
                .keyboardRow(
                    new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                            .text("Регистрация твоего авто")
                            .callbackData(createRegisterCallbackData())
                            .build()
                    )
                ).build()
        ).build();

    try {
      telegramClient.execute(sendMessage);
    } catch (TelegramApiException e) {
      log.error("Error on send message onStart", e);
      throw new RuntimeException(e);
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
    START;

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
