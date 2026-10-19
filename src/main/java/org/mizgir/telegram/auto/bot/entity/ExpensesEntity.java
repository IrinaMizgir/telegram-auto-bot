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
 * Сущность для данных расходов на автомобиль.
 *
 * @author Ирина Мизгир
 * @date 16.10.2026 21:08
 */
@Data
@Entity
@Table(name = "EXPENSES")
@EntityListeners(AuditingEntityListener.class)
public class ExpensesEntity {

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
   * Описание расходов.
   */
  @Column(name = "DESCRIPTION")
  private String description;

  /**
   * Сумма расходов.
   */
  @Column(name = "COST")
  private BigDecimal cost;

  /**
   * Статус расхода.
   */
  private ExpensesStatus status = ExpensesStatus.ENTER_DESCRIPTION;

  /**
   * Статусы расхода.
   */
  public enum ExpensesStatus {
    /**
     * Ввод описания расходов.
     */
    ENTER_DESCRIPTION,

    /**
     * Ввод суммы расходов.
     */
    ENTER_COST,

    /**
     * Завершения ввода данных расходов.
     */
    FINISHED;

    /**
     * Список статусов.
     */
    private static final List<ExpensesStatus> VALUES = List.of(values());

    /**
     * Получение следующего статуса ввода расходов.
     *
     * @param status Статус расходов.
     * @return Следующий статус расходов.
     */
    public static ExpensesStatus getNextStatus(ExpensesStatus status) {
      int currentIndex = VALUES.indexOf(status);

      return VALUES.get(currentIndex + 1);

    }
  }

}
