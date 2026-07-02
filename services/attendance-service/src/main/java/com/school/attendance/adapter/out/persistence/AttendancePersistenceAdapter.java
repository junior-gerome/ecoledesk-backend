package com.school.attendance.adapter.out.persistence;

import com.school.attendance.application.port.in.AttendanceFilter;
import com.school.attendance.application.port.out.AttendanceRepository;
import com.school.attendance.domain.model.AttendanceRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class AttendancePersistenceAdapter implements AttendanceRepository {
    private final AttendanceJpaRepository repository;

    AttendancePersistenceAdapter(AttendanceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AttendanceRecord> findAll(AttendanceFilter filter) {
        return repository.findAll(toSpecification(filter)).stream()
                .map(AttendancePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AttendanceRecord> findById(Long id) {
        return repository.findById(id).map(AttendancePersistenceMapper::toDomain);
    }

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return AttendancePersistenceMapper.toDomain(repository.save(AttendancePersistenceMapper.toEntity(record)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Specification<AttendanceJpaEntity> toSpecification(AttendanceFilter filter) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (filter == null) {
                return predicate;
            }
            if (filter.classId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("classId"), filter.classId()));
            }
            if (filter.date() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("date"), filter.date()));
            }
            if (filter.dateFrom() != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("date"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("date"), filter.dateTo()));
            }
            if (filter.statuses() != null && !filter.statuses().isEmpty()) {
                predicate = builder.and(predicate, root.get("status").in(filter.statuses()));
            }
            if (filter.justified() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("justified"), filter.justified()));
            }
            return predicate;
        };
    }
}
