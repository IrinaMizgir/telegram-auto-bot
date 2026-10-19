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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Сущность заправки.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 14:46
 */
@Data
@Entity
@Table(name = "REFUELS")
@EntityListeners(AuditingEntityListener.class)
public class RefuelEntity {

  /**
   * Идентификатор заправки.
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
   * Сущность связанного автомобиля.
   */
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "AUTO_ID", referencedColumnName = "ID")
  private AutoEntity auto;

  /**
   * Количество литров топлива.
   */
  @Column(name = "AMOUNT")
  private Double amount;

  /**
   * Дистанция пройденная после последней заправки.
   */
  @Column(name = "DISTANCE")
  private Long distance;

  /**
   * Пробег автомобиля.
   */
  @Column
  private Long run;

  /**
   * Стоимость топлива за литр топлива.
   */
  @Column(name = "PRICE")
  private BigDecimal price;

  /**
   * Сумма за топливо.
   */
  @Column(name = "COST")
  private BigDecimal cost;

  /**
   * Статус заправки.
   */
  @Column
  private RefuelStatus status = RefuelStatus.ENTER_RUN;

  /**
   * Статусы заправки.
   */
  public enum RefuelStatus {

    /**
     * Ввод пробега автомобиля.
     */
    ENTER_RUN,

    /**
     * Ввод количество литров топлива.
     */
    ENTER_AMOUNT,

    /**
     * Ввод стоимости топлива за литр.
     */
    ENTER_PRICE,

    /**
     * Завершения ввода данных заправки.
     */
    FINISHED;

    /**
     * Список статусов.
     */
    private static final List<RefuelStatus> VALUES = List.of(values());

    /**
     * Получение следующего статуса ввода заправки.
     *
     * @param status Статус заправки.
     * @return Следующий статус заправки.
     */
    public static RefuelStatus getNextStatus(RefuelStatus status) {
      int currentIndex = VALUES.indexOf(status);

      return VALUES.get(currentIndex + 1);

    }
  }
}
