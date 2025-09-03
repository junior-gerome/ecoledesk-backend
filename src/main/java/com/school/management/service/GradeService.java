package com.school.management.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.exception.ResourceNotFoundException;
import com.school.exception.ValidationException;
import com.school.management.dto.ClassPerformanceReport;
import com.school.management.dto.GradeDTO;
import com.school.management.model.Grade;
import com.school.management.repository.ClasseRoomRepository;
import com.school.management.repository.GradeRepository;
import com.school.management.repository.SequenceRepository;
import com.school.management.repository.StudentRepository;
import com.school.management.repository.SubjectRepository;
import com.school.management.repository.TrimestreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final SequenceRepository sequenceRepository;
    private final TrimestreRepository trimestreRepository;
    private final ClasseRoomRepository classeRepository;
    private final NotificationService notificationService;
    
    // Conversion réutilisable
    private final Function<Grade, GradeDTO> gradeToDtoConverter = this::convertToDTO;

    // 1. Recherches avec pagination et cache
    @Cacheable(value = "grades", key = "'class:' + #classId + ':period:' + #period + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<GradeDTO> findByClassIdAndPeriod(Long classId, String period, Pageable pageable) {
        log.debug("Recherche des notes pour la classe {} et la période {}", classId, period);
        return gradeRepository.findByClasseIdAndPeriod(classId, period, pageable)
                .map(gradeToDtoConverter);
    }

    @Cacheable(value = "grades", key = "'student:' + #studentId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<GradeDTO> findByStudentId(Long studentId, Pageable pageable) {
        log.debug("Recherche des notes pour l'étudiant {}", studentId);
        return gradeRepository.findByStudentId(studentId, pageable)
                .map(gradeToDtoConverter);
    }

    // 2. Opérations CRUD avec cache et validation
    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public GradeDTO createGrade(GradeDTO dto) {
        validateGrade(dto);
        Grade grade = new Grade();
        updateGradeFromDTO(grade, dto);
        Grade savedGrade = gradeRepository.save(grade);
        
        notificationService.notifyNewGrade(
            savedGrade.getStudent().getId(), 
            convertToDTO(savedGrade)
        );
        
        return convertToDTO(savedGrade);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public GradeDTO updateGrade(Long id, GradeDTO dto) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvée"));
        
        validateGrade(dto);
        updateGradeFromDTO(grade, dto);
        return convertToDTO(gradeRepository.save(grade));
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Note non trouvée");
        }
        gradeRepository.deleteById(id);
    }

    // 3. Opérations batch
    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public List<GradeDTO> addBulkGrades(List<GradeDTO> grades) {
        log.debug("Ajout en masse de {} notes", grades.size());
        
        List<Grade> gradesToSave = grades.stream()
            .map(dto -> {
                validateGrade(dto);
                Grade grade = new Grade();
                updateGradeFromDTO(grade, dto);
                return grade;
            })
            .collect(Collectors.toList());

        List<Grade> savedGrades = gradeRepository.saveAll(gradesToSave);
        
        // Notifications groupées
        savedGrades.forEach(grade -> 
            notificationService.notifyNewGrade(
                grade.getStudent().getId(), 
                convertToDTO(grade))
        );
        
        return savedGrades.stream()
                .map(gradeToDtoConverter)
                .collect(Collectors.toList());
    }

    // 4. Méthodes de calcul
    @Transactional(readOnly = true)
    public BigDecimal calculateGeneralAverage(Long studentId) {
        return gradeRepository.calculateAverageByStudentId(studentId)
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public Integer calculateStudentRank(Long studentId, Long classId) {
        return gradeRepository.calculateStudentRank(studentId, classId)
                .orElse(0);
    }

    // 5. Recherches spécialisées
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(gradeToDtoConverter)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesBySubjectAndSequence(Long subjectId, Long sequenceId) {
        return gradeRepository.findBySubjectIdAndSequenceId(subjectId, sequenceId).stream()
                .map(gradeToDtoConverter)
                .collect(Collectors.toList());
    }

    // Méthodes utilitaires
    private void validateGrade(GradeDTO dto) {
        List<String> errors = new ArrayList<>();
        
        // Validation des données obligatoires
        if (dto.getStudent().getId() == null) {
            errors.add("L'identifiant de l'élève est obligatoire");
        }
        
        if (dto.getSubject().getId() == null) {
            errors.add("L'identifiant de la matière est obligatoire");
        }
        
        if (dto.getClasse().getId()== null) {
            errors.add("L'identifiant de la classe est obligatoire");
        }
        
        if (dto.getSequence().getId() == null) {
            errors.add("L'identifiant de la séquence est obligatoire");
        }
        
        if (dto.getGrade() == null) {
            errors.add("La note est obligatoire");
        } else if (dto.getGrade().compareTo(BigDecimal.ZERO) < 0 || 
                  dto.getGrade().compareTo(BigDecimal.valueOf(20)) > 0) {
            errors.add("La note doit être comprise entre 0 et 20");
        }
        
        if (dto.getAssessmentType() == null) {
            errors.add("Le type d'évaluation est obligatoire");
        }
        
        if (dto.getCoefficient() != null && dto.getCoefficient().compareTo(BigDecimal.valueOf(0.1)) < 0) {
            errors.add("Le coefficient doit être supérieur à 0.1");
        }
        
        if (dto.getComments() != null && dto.getComments().length() > 1000) {
            errors.add("Le commentaire ne doit pas dépasser 1000 caractères");
        }

        // Vérification de l'unicité
        if (dto.getId() == null && // Seulement pour les nouvelles notes
            gradeRepository.existsByStudentIdAndSubjectIdAndSequenceId(dto.getStudent().getId(), 
                                                                      dto.getSubject().getId(), 
                                                                      dto.getSequence().getId())) {
            errors.add("Une note existe déjà pour cet élève, cette matière et cette séquence");
        }
       
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void updateGradeFromDTO(Grade grade, GradeDTO dto) {
        grade.setGrade(dto.getGrade());
        grade.setPedagogicalComment(dto.getComments());
        grade.setAssessmentType(dto.getAssessmentType());
        grade.setCoefficient(dto.getCoefficient());
        grade.setAssessmentDate(dto.getAssessmentDate());
        
        grade.setStudent(studentRepository.findById(dto.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé")));
        
        grade.setSubject(subjectRepository.findByNameSubject(dto.getSubject().getNameSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Matière non trouvée")));
        
        grade.setSequence(sequenceRepository.findByLibelleSequence(dto.getSequence().getLibelleSequence())
                .orElseThrow(() -> new ResourceNotFoundException("Séquence non trouvée")));
        
        if (dto.getTrimestre() != null) {
            grade.setTrimestre(trimestreRepository.findByLibelleTrimestre(dto.getTrimestre().getLibelleTrimestre())
                    .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouvé")));
        }
        
        grade.setClasse(classeRepository.findByNameClasse(dto.getClasse().getNameClasse())
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée")));
    }

    private GradeDTO convertToDTO(Grade grade) {
        return GradeDTO.builder()
                .id(grade.getId())
                .grade(grade.getGrade())
                .student(grade.getStudent())
                .subject(grade.getSubject())
                .assessmentType(grade.getAssessmentType())
                .coefficient(grade.getCoefficient())
                .sequence(grade.getSequence())
                .trimestre(grade.getTrimestre())
                .classe(grade.getClasse())
                .comments(grade.getPedagogicalComment())
                .assessmentDate(grade.getAssessmentDate())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }

    public ClassPerformanceReport generateClassPerformanceReport(Long classId) {
        // TODO: Implement class performance report generation logic
        return new ClassPerformanceReport();
    }

    @CacheEvict(value = "grades", allEntries = true)
    public void clearGradesCache() {
        log.debug("Vidage du cache des notes");
    }

    // Le logger fonctionnera maintenant
    public void someMethod() {
        log.debug("Message de log");
    }
}