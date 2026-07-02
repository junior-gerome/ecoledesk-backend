package com.school.platform.staff.infrastructure.persistence;

import com.school.platform.staff.domain.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByCode(String code);
    boolean existsByCode(String code);
}
