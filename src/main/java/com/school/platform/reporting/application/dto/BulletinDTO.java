package com.school.platform.reporting.application.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.school.platform.reporting.application.dto.SubjectGradeDTO;

@Builder
@Data
public class BulletinDTO {
    private Long studentId;
    private String lastNameStudent;
    private String firstNameStudent;
    private String NameClasse;
    private String libelleTrimestre;
    private List<SubjectGradeDTO> grades;
    private BigDecimal moyenneGenerale;
    private Integer rang;
    private String appreciation;
    private Map<String, Integer> statistiques;

}
