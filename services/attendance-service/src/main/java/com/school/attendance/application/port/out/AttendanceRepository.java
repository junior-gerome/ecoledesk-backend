package com.school.attendance.application.port.out;

import com.school.attendance.application.port.in.AttendanceFilter;
import com.school.attendance.domain.model.AttendanceRecord;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    List<AttendanceRecord> findAll(AttendanceFilter filter);
    Optional<AttendanceRecord> findById(Long id);
    AttendanceRecord save(AttendanceRecord record);
    void deleteById(Long id);
}
