package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Сущность пользователя автомобиля.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 15:35
 */
@Data
@Entity
@Table(name = "USERS")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

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
