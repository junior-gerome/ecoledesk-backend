package com.school.platform.academic.application.dto.teacher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherScheduleResponse {
    private Long teacherId;
    private String teacherName;
    private Long subjectId;
    private String subjectName;
    private Long classId;
    private String className;
    private String schoolYear;
    private String day;
    private String startTime;
    private String endTime;
}
