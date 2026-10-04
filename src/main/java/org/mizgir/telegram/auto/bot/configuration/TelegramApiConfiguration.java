package org.mizgir.telegram.auto.bot.configuration;

import org.mizgir.telegram.auto.bot.configuration.property.TelegramBotConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Конфигурация для API telegram bot.
 *
 * @author Ирина Мизгир
 * @date 04.10.2026 18:50
 */
@Configuration
public class TelegramApiConfiguration {

  @Bean
  TelegramClient telegramClient(TelegramBotConfigProperties telegramBotConfigProperties) {
    return new OkHttpTelegramClient(telegramBotConfigProperties.token());
  }
}
