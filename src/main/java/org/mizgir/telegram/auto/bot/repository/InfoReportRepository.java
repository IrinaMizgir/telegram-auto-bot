package org.mizgir.telegram.auto.bot.repository;

import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.InfoReportEntity;
import org.mizgir.telegram.auto.bot.entity.InfoReportEntity.InfoReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными отчёта за период
 * @author Ирина Мизгир
 * @date 18.10.2026 13:15
 */

@Repository
public interface InfoReportRepository extends JpaRepository<InfoReportEntity, Integer> {

  InfoReportEntity getByAutoIdAndStatusNot(Integer autoId, InfoReportStatus status);

  Optional<InfoReportEntity> findByAutoIdAndStatusNot(Integer autoId, InfoReportStatus status);

}
