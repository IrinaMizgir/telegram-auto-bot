package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Сущность пользователя автомобиля.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 15:35
 */
@Data
@Entity
@Table(name = "USERS")
public class UserEntity {

  /**
   * Идентификатор пользователя телеграма.
   */
  @Id
  private Long id;

  /**
   * Имя пользователя.
   */
  @Column(name = "USER_NAME")
  private String userName;

  /**
   * Сущность связанного автомобиля.
   */
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private AutoEntity auto;
}
