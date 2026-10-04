package org.mizgir.telegram.auto.bot.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки конфигурации для telegram bot.
 *
 * @param name  Имя бота.
 * @param token Токен доступа к API telegram bot.
 * @author Ирина Мизгир
 * @date 03.10.2026 18:18
 */
@ConfigurationProperties("app.telegram.bot")
public record TelegramBotConfigProperties(String name, String token) {

}
