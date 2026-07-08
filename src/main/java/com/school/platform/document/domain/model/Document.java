package com.school.platform.document.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.school.platform.enrollment.domain.model.Student;

import com.school.platform.document.domain.model.TypeDocument;

@Data
@Entity
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false)
    private TypeDocument typeDocument;

    @Column(name = "chemin_fichier", nullable = false, length = 255)
    private String cheminFichier;

    @Column(name = "date_ajout", insertable = false, updatable = false)
    private LocalDateTime dateAjout;

    
}
