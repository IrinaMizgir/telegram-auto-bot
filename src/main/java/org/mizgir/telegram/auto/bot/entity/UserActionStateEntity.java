package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Сущность состояние действия текущего пользователя.
 *
 * @author Ирина Мизгир
 * @date 10.10.2026 15:28
 */
@Entity
@Data
@Table(name = "USER_ACTION_STATES")
@EntityListeners(AuditingEntityListener.class)
public class UserActionStateEntity {

  /**
   * Идентификатор пользователя телеграма.
   */
  @Id
  private Long id;

  /**
   * Дата создания.
   */
  @CreatedDate
  @Column(name = "CREATION_DATE")
  private LocalDate creationDate;

  /**
   * Дата последнее изменения.
   */
  @LastModifiedDate
  @Column(name = "LAST_MODIFIED_DATE")
  private LocalDate lastModifiedDate;

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
    ENTER_MAINTENANCE,

    /**
     * Ввод данных расхода на автомобиль.
     */
    ENTER_EXPENSES,

    /**
     * Ввод данных о периоде.
     */
    ENTER_INFO_BY_PERIOD,

    /**
     * Ввод данных о годе.
     */
    ENTER_INFO_BY_YEAR,

    /**
     * Ввод данных о месяце.
     */
    ENTER_INFO_BY_MONTH,

    /**
     * Ввод данных о дне.
     */
    ENTER_INFO_BY_DAY
  }

}
