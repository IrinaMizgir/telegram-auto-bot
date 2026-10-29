package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Сущность автомобиля.
 *
 * @author Ирина Мизгир
 * @date 07.10.2026 21:45
 */
@Data
@Entity
@Table(name = "AUTOS")
@EntityListeners(AuditingEntityListener.class)
public class AutoEntity {

  /**
   * Идентификатор автомобиля.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

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
   * Модель и марка автомобиля.
   */
  @Column
  private String name;

  /**
   * Год выпуска автомобиля.
   */
  @Column(name = "\"YEAR\"")
  private Year year;

  /**
   * Пробег автомобиля.
   */
  @Column
  private Long run;

  /**
   * Статус регистрации.
   */
  @Column
  private RegistrationStatus status = RegistrationStatus.ENTER_NAME;

  /**
   * Сущность связанного пользователя.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
  private UserEntity user;

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
