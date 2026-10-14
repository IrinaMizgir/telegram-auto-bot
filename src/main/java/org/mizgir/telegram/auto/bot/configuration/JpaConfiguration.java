package org.mizgir.telegram.auto.bot.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Конфигурация для JPA.
 * @author Ирина Мизгир
 * @date 11.10.2026 21:55
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.mizgir.telegram.auto.bot.repository")
@EnableTransactionManagement
public class JpaConfiguration {

  @Bean
  JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

}
