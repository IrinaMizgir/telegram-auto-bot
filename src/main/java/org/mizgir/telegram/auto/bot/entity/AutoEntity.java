package org.mizgir.telegram.auto.bot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Year;
import lombok.Data;

/**
 * Сущность автомобиля.
 *
 * @author Ирина Мизгир
 * @date 07.10.2026 21:45
 */
@Data
@Entity
@Table(name = "AUTOS")
public class AutoEntity {

  /**
   * Идентификатор автомобиля.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

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
   * Сущность связанного пользователя.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
  private UserEntity user;

}
