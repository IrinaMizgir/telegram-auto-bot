package org.mizgir.telegram.auto.bot.repository;

import java.util.Optional;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity;
import org.mizgir.telegram.auto.bot.entity.RefuelEntity.RefuelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с данными заправки автомобиля.
 *
 * @author Ирина Мизгир
 * @date 11.10.2026 15:19
 */
@Repository
public interface RefuelRepository extends JpaRepository<RefuelEntity, Integer> {

  RefuelEntity getByAutoIdAndStatusNot(Integer autoId, RefuelStatus status);

  Optional<RefuelEntity> findByAutoIdAndStatusNot(Integer autoId, RefuelStatus status);
}
