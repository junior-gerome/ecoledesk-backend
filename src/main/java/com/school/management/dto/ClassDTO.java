package com.school.management.dto;

import com.school.management.model.*;

import lombok.Data;

@Data
public class ClassDTO {
    private Long id;
    private String nameClasse;
    private String levell;
    private Integer capacity;
    private Teacher teacher;    
    private AnneeScolaire anneeScolaire;
    private String description;
    //private Boolean active;
    private Section section;

    
}
