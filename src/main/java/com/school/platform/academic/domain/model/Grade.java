package com.school.platform.academic.domain.model;

import com.school.platform.academic.domain.enums.GradeStatus;

import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.identityaccess.domain.model.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"student", "subject", "classe", "sequence", "trimestre"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "academic_grade")
public class Grade extends BaseEntity {
    
        // Relation vers l'élève (obligatoire)
    @NotNull(message = "L'élève est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Relation vers la matière (obligatoire)
    @NotNull(message = "La matière est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Relation vers la classe (obligatoire)
    @NotNull(message = "La classe est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClasseRoom classe;

    // Relation vers la séquence pédagogique (obligatoire)
    @NotNull(message = "La séquence est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    private Sequence sequence;

    // Relation vers le trimestre (optionnel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trimestre_id")
    private Trimestre trimestre;

   
    // Valeur de la note (ex : 15.50, 4.75, etc.)
    @NotNull(message = "La note est obligatoire")
    @DecimalMin(value = "0.00", message = "La note ne peut pas être négative")
    @DecimalMax(value = "20.00", message = "La note ne peut pas dépasser 20")
    @Digits(integer = 2, fraction = 2, message = "Format de note invalide (max 2 chiffres avant la virgule, 2 après)")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal grade;

    // Coefficient de la note
    @DecimalMin(value = "0.1", message = "Le coefficient doit etre au moins 0.1")
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal coefficient = BigDecimal.ONE;

    // Commentaires pédagogiques
    @Column(name = "pedagogical_comment", length = 1000)
    private String pedagogicalComment;

    // Date de l'évaluation (différente de la création)
    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    // Version pour le contrôle d'accès concurrentiel
    @Version
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GradeStatus status = GradeStatus.DRAFT;

    private String period;
}



