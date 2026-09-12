package com.school.platform.academic.application.dto.classeroom;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.school.platform.academic.application.dto.section.SectionDTO;
import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import lombok.Data;

@Data
public class ClasseRoomDTO {
    private Long id;
    private String nameClasse;
    private String level;
    private SectionDTO section;
    private Integer capacity;
    private StaffMemberBasicDTO teacher;
    /**
     * Canonical backend field name. Also accepts "anneeScolaire" from legacy frontend payloads.
     */
    @JsonAlias("anneeScolaire")
    private AcademicYearDTO academicYear;
    private String description;
}


