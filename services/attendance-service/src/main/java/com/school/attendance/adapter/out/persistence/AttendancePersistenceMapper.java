package com.school.attendance.adapter.out.persistence;

import com.school.attendance.domain.model.AttendanceRecord;

final class AttendancePersistenceMapper {
    private AttendancePersistenceMapper() {
    }

    static AttendanceRecord toDomain(AttendanceJpaEntity entity) {
        return new AttendanceRecord(entity.id, entity.studentId, entity.studentName, entity.classId,
                entity.className, entity.date, entity.status, entity.hours, entity.justified,
                entity.justificationNote, entity.updatedAt);
    }

    static AttendanceJpaEntity toEntity(AttendanceRecord record) {
        AttendanceJpaEntity entity = new AttendanceJpaEntity();
        entity.id = record.id();
        entity.studentId = record.studentId();
        entity.studentName = record.studentName();
        entity.classId = record.classId();
        entity.className = record.className();
        entity.date = record.date();
        entity.status = record.status();
        entity.hours = record.hours();
        entity.justified = record.justified();
        entity.justificationNote = record.justificationNote();
        return entity;
    }
}
