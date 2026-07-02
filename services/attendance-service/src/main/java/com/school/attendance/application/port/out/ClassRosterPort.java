package com.school.attendance.application.port.out;

import java.util.List;

public interface ClassRosterPort {
    List<ClassRosterStudent> findStudentsByClass(Long classId, Long schoolYearId);
}
