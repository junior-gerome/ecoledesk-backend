package com.school.platform.academic.application.dto;

import lombok.Data;

@Data
public class ClasseRoomDTO {
    private Long id;
    private String nameClasse;
    private String level;
    private SectionDTO section;
    private Integer capacity;
    private TeacherDTO teacher;    
    private AnneeScolaireDTO anneeScolaire;
    private String description;
    

    
}


