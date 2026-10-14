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
import java.util.List;
import lombok.Data;

/**
 * Сущность для данных сервисного обслуживания.
 * @author Ирина Мизгир
 * @date 12.10.2026 21:27
 */
@Data
@Entity
@Table(name = "MAINTENANCE")
public class MaintenanceEntity {

  /**
   * Идентификатор СО.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Сущность связанного автомобиля.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "AUTO_ID", referencedColumnName = "ID")
  private AutoEntity auto;

  /**
   * Дистанция пройденная после последней СО.
   */
  @Column(name = "DISTANCE")
  private Long distance;

  /**
   * Пробег автомобиля.
   */
  @Column
  private Long run;

  /**
   * Сумма за СО.
   */
  @Column(name = "COST")
  private Double cost;

  /**
   * Описание работ СО.
   */
  @Column(name = "DESCRIPTION")
  private String description;

  /**
   * Статус СО.
   */
  @Column
  private MaintenanceStatus status = MaintenanceStatus.ENTER_RUN;

  public enum MaintenanceStatus {
    /**
     * Ввод пробега автомобиля.
     */
    ENTER_RUN,

    /**
     * Ввод описания работ СО.
     */
    ENTER_DESCRIPTION,

    /**
     * Ввод суммы СО.
     */
    ENTER_COST,

    /**
     * Завершения ввода данных СО.
     */
    FINISHED;

    /**
     * Список статусов.
     */
    private static final List<MaintenanceStatus> VALUES = List.of(values());

    /**
     * Получение следующего статуса ввода СО.
     *
     * @param status Статус СО.
     * @return Следующий статус СО.
     */
    public static MaintenanceStatus getNextStatus(MaintenanceStatus status) {
      int currentIndex = VALUES.indexOf(status);

      return VALUES.get(currentIndex + 1);

    }

  }
}
