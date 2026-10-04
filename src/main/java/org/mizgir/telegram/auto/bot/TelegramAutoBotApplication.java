package org.mizgir.telegram.auto.bot;

import org.mizgir.telegram.auto.bot.configuration.property.TelegramBotConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramBotConfigProperties.class)
public class TelegramAutoBotApplication {

  static void main(String[] args) {
    SpringApplication.run(TelegramAutoBotApplication.class, args);
  }

}
