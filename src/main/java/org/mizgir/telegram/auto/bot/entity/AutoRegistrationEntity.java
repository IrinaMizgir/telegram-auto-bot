package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;

/**
 * Сущность регистрации автомобиля.
 *
 * @author Ирина Мизгир
 * @date 07.10.2026 21:38
 */
@Data
@Entity
@Table(name = "AUTO_REGISTRATIONS")
public class AutoRegistrationEntity {

  /**
   * Идентификатор пользователя телеграма.
   */
  @Id
  private Long telegramUserId;

  /**
   * Статус регистрации.
   */
  @Column
  private RegistrationStatus status = RegistrationStatus.ENTER_NAME;

  /**
   * Сущность связанного автомобиля.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "AUTO_ID", referencedColumnName = "ID")
  private AutoEntity auto;

  /**
   * Статусы регистрации.
   */
  public enum RegistrationStatus {
    /**
     * Ввод модели и марки автомобиля.
     */
    ENTER_NAME,

    /**
     * Ввод года выпуска автомобиля.
     */
    ENTER_YEAR,

    /**
     * Ввод пробега автомобиля.
     */
    ENTER_RUN,

    /**
     * Завершения ввода данных автомобиля.
     */
    FINISHED;

    /**
     * Список статусов.
     */
    private static final List<RegistrationStatus> VALUES = List.of(values());

    /**
     * Получение следующего статуса регистрации.
     *
     * @param status Статус регистрации.
     * @return Следующий статус регистрации.
     */
    public static RegistrationStatus getNextStatus(RegistrationStatus status) {
      int currentIndex = VALUES.indexOf(status);

      return VALUES.get(currentIndex + 1);

    }
  }

}
