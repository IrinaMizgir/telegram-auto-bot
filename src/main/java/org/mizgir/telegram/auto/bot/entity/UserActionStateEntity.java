package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Сущность состояние действия текущего пользователя.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 15:28
 */
@Entity
@Data
@Table(name = "USER_ACTION_STATES")
public class UserActionStateEntity {

  /**
   * Идентификатор пользователя телеграма.
   */
  @Id
  private Long id;

  /**
   * Статус регистрации.
   */
  @Column
  private ActionType type = ActionType.DEFAULT;

  /**
   * Сущность связанного пользователя.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
  private UserEntity user;

  /**
   * Тип действия.
   */
  public enum ActionType {

    /**
     * Действие по умолчанию.
     */
    DEFAULT,

    /**
     * Регистрация авто.
     */
    REGISTER_AUTO,

    /**
     * Ввод данных заправки.
     */
    ENTER_REFUEL,

    /**
     * Ввод данных СО.
     */
    ENTER_MAINTENANCE
  }

}
