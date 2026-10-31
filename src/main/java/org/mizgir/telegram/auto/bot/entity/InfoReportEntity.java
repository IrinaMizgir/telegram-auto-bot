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
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Сущность отчёта за период.
 *
 * @author Ирина Мизгир
 * @date 18.10.2026 12:46
 */
@Entity
@Data
@Table(name = "INFO_REPORT")
@EntityListeners(AuditingEntityListener.class)
public class InfoReportEntity {

  /**
   * Идентификатор отчёта.
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
   * Дата начала периода.
   */
  @Column(name = "PERIOD_START_DATE")
  private LocalDate periodStartDate;

  /**
   * Дата окончания периода.
   */
  @Column(name = "PERIOD_END_DATE")
  private LocalDate periodEndDate;

  /**
   * Сущность связанного автомобиля.
   */
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "AUTO_ID", referencedColumnName = "ID")
  private AutoEntity auto;

  /**
   * Статус заправки.
   */
  @Column
  private InfoReportStatus status = InfoReportStatus.ENTER_START_DATE;

  /**
   * Статусы отчёта.
   */
  public enum InfoReportStatus {
    /**
     * Ввод даты начала периода.
     */
    ENTER_START_DATE,

    /**
     * Ввод даты окончания периода.
     */
    ENTER_END_DATE,

    /**
     * Завершения ввода отчёта за период.
     */
    FINISHED;

    /**
     * Список статусов.
     */
    private static final List<InfoReportStatus> VALUES = List.of(values());

    /**
     * Получение следующего статуса ввода.
     *
     * @param status Статус.
     * @return Следующий статус.
     */
    public static InfoReportStatus getNextStatus(InfoReportStatus status) {
      return VALUES.get(VALUES.indexOf(status) + 1);

    }
  }
}
