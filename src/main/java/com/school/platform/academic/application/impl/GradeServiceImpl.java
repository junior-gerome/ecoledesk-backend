package com.school.platform.academic.application.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.shared.domain.exception.ValidationException;
import com.school.platform.notification.application.NotificationService;
import com.school.platform.shared.application.BusinessAuditService;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.reporting.application.dto.ClassPerformanceReport;
import com.school.platform.academic.application.dto.BulkGradeCreateRequest;
import com.school.platform.academic.application.dto.BulkGradeItemRequest;
import com.school.platform.academic.application.dto.GradeBatchStatusRequest;
import com.school.platform.academic.application.dto.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.GradeResponseDTO;
import com.school.platform.academic.application.interfaces.GradeService;
import com.school.platform.academic.application.mapper.GradeMapper;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.academic.domain.enums.GradeStatus;
import com.school.platform.academic.domain.model.Sequence;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.academic.domain.model.Subject;
import com.school.platform.academic.domain.model.Trimestre;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.academic.infrastructure.persistence.SequenceRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.academic.infrastructure.persistence.SubjectRepository;
import com.school.platform.academic.infrastructure.persistence.TrimestreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final SequenceRepository sequenceRepository;
    private final TrimestreRepository trimestreRepository;
    private final ClasseRoomRepository classeRepository;
    private final NotificationService notificationService;
    private final GradeMapper gradeMapper;
    private final BusinessAuditService businessAuditService;

    @Cacheable(value = "grades", key = "'class:' + #classId + ':period:' + #period + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<GradeResponseDTO> findByClassIdAndPeriod(Long classId, String period, Pageable pageable) {
        log.debug("Recherche des notes pour la classe {} et la periode {}", classId, period);
        return gradeRepository.findByClasseIdAndPeriod(classId, period, pageable)
                .map(gradeMapper::toResponseDto);
    }

    @Cacheable(value = "grades", key = "'student:' + #studentId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<GradeResponseDTO> findByStudentId(Long studentId, Pageable pageable) {
        log.debug("Recherche des notes pour l'etudiant {}", studentId);
        return gradeRepository.findByStudentId(studentId, pageable)
                .map(gradeMapper::toResponseDto);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public GradeResponseDTO save(GradeCreateRequestDTO dto) {
        validateRequest(dto, null);
        Grade grade = gradeMapper.toEntity(dto);
        applyRelationsAndValues(grade, dto);
        Grade saved = gradeRepository.save(grade);
        businessAuditService.record("GRADE_CREATED", "academic_grade", saved.getId());

        GradeResponseDTO response = gradeMapper.toResponseDto(saved);
        notificationService.notifyNewGrade(saved.getStudent().getId(), response);
        return response;
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public GradeResponseDTO createGrade(GradeCreateRequestDTO dto) {
        return save(dto);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public GradeResponseDTO updateGrade(Long id, GradeCreateRequestDTO dto) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvee"));

        ensureGradeEditable(existing);
        validateRequest(dto, id);
        gradeMapper.updateEntityFromRequest(dto, existing);
        applyRelationsAndValues(existing, dto);

        Grade saved = gradeRepository.save(existing);
        businessAuditService.record("GRADE_UPDATED", "academic_grade", saved.getId());
        return gradeMapper.toResponseDto(saved);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public void deleteGrade(Long id) {
        Grade existing = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvee"));
        if (currentStatus(existing) == GradeStatus.LOCKED) {
            throw new BadRequestException("Impossible de supprimer une note verrouillee.");
        }
        gradeRepository.delete(existing);
        businessAuditService.record("GRADE_DELETED", "academic_grade", id);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public List<GradeResponseDTO> addBulkGrades(BulkGradeCreateRequest request) {
        if (request == null || request.getGrades() == null || request.getGrades().isEmpty()) {
            throw new BadRequestException("Au moins une note est obligatoire.");
        }

        List<GradeCreateRequestDTO> grades = request.getGrades().stream()
                .map(item -> toGradeCreateRequest(request, item))
                .collect(Collectors.toList());
        return addBulkGrades(grades);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public GradeResponseDTO validateGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvee"));

        if (currentStatus(grade) == GradeStatus.LOCKED) {
            throw new BadRequestException("Impossible de valider une note verrouillee.");
        }
        if (currentStatus(grade) == GradeStatus.DRAFT) {
            grade.setStatus(GradeStatus.VALIDATED);
            grade = gradeRepository.save(grade);
            businessAuditService.record("GRADE_VALIDATED", "academic_grade", grade.getId());
        }
        return gradeMapper.toResponseDto(grade);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public List<GradeResponseDTO> validateGradesByClass(GradeBatchStatusRequest request) {
        List<Grade> grades = findMatchingGrades(request);
        ensureGradesFound(grades);

        if (grades.stream().anyMatch(grade -> currentStatus(grade) == GradeStatus.LOCKED)) {
            throw new BadRequestException("Impossible de valider un lot contenant des notes verrouillees.");
        }

        grades.stream()
                .filter(grade -> currentStatus(grade) == GradeStatus.DRAFT)
                .forEach(grade -> grade.setStatus(GradeStatus.VALIDATED));

        List<Grade> saved = gradeRepository.saveAll(grades);
        businessAuditService.record("GRADE_BATCH_VALIDATED", "academic_grade", request.getClasseId());
        return gradeMapper.toResponseDto(saved);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public List<GradeResponseDTO> lockGradesByClass(GradeBatchStatusRequest request) {
        List<Grade> grades = findMatchingGrades(request);
        ensureGradesFound(grades);

        if (grades.stream().anyMatch(grade -> currentStatus(grade) == GradeStatus.DRAFT)) {
            throw new BadRequestException("Toutes les notes doivent etre validees avant verrouillage.");
        }

        grades.stream()
                .filter(grade -> currentStatus(grade) == GradeStatus.VALIDATED)
                .forEach(grade -> grade.setStatus(GradeStatus.LOCKED));

        List<Grade> saved = gradeRepository.saveAll(grades);
        businessAuditService.record("GRADE_BATCH_LOCKED", "academic_grade", request.getClasseId());
        return gradeMapper.toResponseDto(saved);
    }

    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public List<GradeResponseDTO> unlockGradesByClass(GradeBatchStatusRequest request) {
        requireUnlockJustification(request);
        List<Grade> grades = findMatchingGrades(request);
        ensureGradesFound(grades);

        grades.stream()
                .filter(grade -> currentStatus(grade) == GradeStatus.LOCKED)
                .forEach(grade -> grade.setStatus(GradeStatus.VALIDATED));

        List<Grade> saved = gradeRepository.saveAll(grades);
        businessAuditService.record("GRADE_BATCH_UNLOCKED", "academic_grade", request.getClasseId());
        return gradeMapper.toResponseDto(saved);
    }
    @Transactional
    @CacheEvict(value = "grades", allEntries = true)
    public List<GradeResponseDTO> addBulkGrades(List<GradeCreateRequestDTO> grades) {
        if (grades == null || grades.isEmpty()) {
            return List.of();
        }

        List<Grade> entities = new ArrayList<>();
        for (GradeCreateRequestDTO dto : grades) {
            validateRequest(dto, null);
            Grade grade = gradeMapper.toEntity(dto);
            applyRelationsAndValues(grade, dto);
            entities.add(grade);
        }

        List<Grade> savedGrades = gradeRepository.saveAll(entities);
        savedGrades.forEach(saved -> businessAuditService.record("GRADE_CREATED", "academic_grade", saved.getId()));
        List<GradeResponseDTO> responses = savedGrades.stream()
                .map(gradeMapper::toResponseDto)
                .collect(Collectors.toList());

        responses.forEach(response -> notificationService.notifyNewGrade(response.getStudentId(), response));
        return responses;
    }

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

    @Transactional(readOnly = true)
    @Cacheable(value = "grades", key = "'student:list:' + #studentId")
    public List<GradeResponseDTO> getGradesByStudent(Long studentId) {
        return gradeMapper.toResponseDto(gradeRepository.findByStudentId(studentId));
    }

    @Transactional(readOnly = true)
    public GradeResponseDTO getGradeById(Long id) {
        return gradeRepository.findById(id)
                .map(gradeMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Note non trouvee"));
    }

    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getGradesBySubjectAndSequence(Long subjectId, Long sequenceId) {
        return gradeMapper.toResponseDto(gradeRepository.findBySubjectIdAndSequenceId(subjectId, sequenceId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentGradeStats(Long studentId, Long subjectId, String period) {
        List<Grade> studentGrades = gradeRepository.findByStudentIdAndSubjectId(studentId, subjectId).stream()
                .filter(grade -> periodMatches(grade, period))
                .collect(Collectors.toList());

        Long classId = studentGrades.stream()
                .map(Grade::getClasse)
                .filter(classe -> classe != null && classe.getId() != null)
                .map(ClasseRoom::getId)
                .findFirst()
                .orElse(null);

        List<Grade> classSubjectGrades = classId == null
                ? List.of()
                : gradeRepository.findGradesWithSequenceByClassAndSubject(classId, subjectId).stream()
                        .filter(grade -> periodMatches(grade, period))
                        .collect(Collectors.toList());

        List<Map.Entry<Long, Double>> rankedStudents = rankedStudents(classSubjectGrades);
        int rank = 0;
        for (int i = 0; i < rankedStudents.size(); i++) {
            if (rankedStudents.get(i).getKey().equals(studentId)) {
                rank = i + 1;
                break;
            }
        }

        return Map.of(
                "average", average(studentGrades),
                "highest", max(studentGrades),
                "lowest", min(studentGrades),
                "classAverage", average(classSubjectGrades),
                "rank", rank,
                "totalStudents", rankedStudents.size());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClassGradeStats(Long classId, Long subjectId, String period) {
        List<Grade> grades = gradeRepository.findGradesWithSequenceByClassAndSubject(classId, subjectId).stream()
                .filter(grade -> periodMatches(grade, period))
                .collect(Collectors.toList());

        return Map.of(
                "average", average(grades),
                "highest", max(grades),
                "lowest", min(grades),
                "classAverage", average(grades),
                "rank", 0,
                "totalStudents", rankedStudents(grades).size());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentReport(Long studentId, String period) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Eleve non trouve"));

        List<Grade> grades = gradeRepository.findByStudentId(studentId).stream()
                .filter(grade -> periodMatches(grade, period))
                .collect(Collectors.toList());

        ClasseRoom classe = grades.stream()
                .map(Grade::getClasse)
                .filter(item -> item != null && item.getId() != null)
                .findFirst()
                .orElse(null);

        List<Map<String, Object>> subjectAverages = grades.stream()
                .filter(grade -> grade.getSubject() != null && grade.getSubject().getId() != null)
                .collect(Collectors.groupingBy(
                        grade -> grade.getSubject().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .values()
                .stream()
                .map(this::subjectAverageRow)
                .collect(Collectors.toList());

        double weightedTotal = subjectAverages.stream()
                .mapToDouble(row -> number(row.get("weightedAverage")))
                .sum();
        double totalCoefficient = subjectAverages.stream()
                .mapToDouble(row -> number(row.get("coefficient")))
                .sum();
        double generalAverage = totalCoefficient == 0 ? average(grades) : weightedTotal / totalCoefficient;

        int rank = classe == null ? 0 : calculateStudentRank(studentId, classe.getId());
        int totalStudents = classe == null
                ? 0
                : rankedStudents(gradeRepository.findByClasseId(classe.getId()).stream()
                        .filter(grade -> periodMatches(grade, period))
                        .collect(Collectors.toList())).size();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("studentId", student.getId());
        report.put("studentName", buildStudentName(student));
        report.put("classId", classe == null ? null : classe.getId());
        report.put("sectionId", classe == null || classe.getSection() == null ? null : classe.getSection().getId());
        report.put("className", classe == null ? "" : classe.getNameClasse());
        report.put("period", period);
        report.put("subjectAverages", subjectAverages);
        report.put("generalAverage", generalAverage);
        report.put("rank", rank);
        report.put("totalStudents", totalStudents);
        report.put("teacherComments", teacherComment(generalAverage));
        report.put("principalComments", "");
        return report;
    }

    private GradeCreateRequestDTO toGradeCreateRequest(BulkGradeCreateRequest request, BulkGradeItemRequest item) {
        GradeCreateRequestDTO dto = new GradeCreateRequestDTO();
        dto.setStudentId(item.getStudentId());
        dto.setClasseId(request.getClasseId());
        dto.setSubjectId(request.getSubjectId());
        dto.setSequenceId(request.getSequenceId());
        dto.setTrimestreId(request.getTrimestreId());
        dto.setScore(item.getScore());
        dto.setCoefficient(item.getCoefficient());
        dto.setPeriod(request.getPeriod());
        dto.setAssessmentDate(request.getAssessmentDate());
        dto.setComments(item.getComments());
        return dto;
    }

    private void ensureGradeEditable(Grade grade) {
        GradeStatus status = currentStatus(grade);
        if (status == GradeStatus.LOCKED) {
            throw new BadRequestException("Impossible de modifier une note verrouillee.");
        }
        if (status == GradeStatus.VALIDATED) {
            throw new BadRequestException("Impossible de modifier une note deja validee.");
        }
    }

    private List<Grade> findMatchingGrades(GradeBatchStatusRequest request) {
        validateBatchRequest(request);
        return gradeRepository.findByClasseId(request.getClasseId()).stream()
                .filter(grade -> request.getSubjectId() == null
                        || grade.getSubject() != null && request.getSubjectId().equals(grade.getSubject().getId()))
                .filter(grade -> request.getSequenceId() == null
                        || grade.getSequence() != null && request.getSequenceId().equals(grade.getSequence().getId()))
                .filter(grade -> periodMatches(grade, request.getPeriod()))
                .collect(Collectors.toList());
    }

    private void validateBatchRequest(GradeBatchStatusRequest request) {
        if (request == null || request.getClasseId() == null || request.getClasseId() <= 0) {
            throw new BadRequestException("La classe est obligatoire pour ce traitement.");
        }
        if (request.getSubjectId() != null && request.getSubjectId() <= 0) {
            throw new BadRequestException("L'identifiant de la matiere doit etre positif.");
        }
        if (request.getSequenceId() != null && request.getSequenceId() <= 0) {
            throw new BadRequestException("L'identifiant de la sequence doit etre positif.");
        }
    }

    private void ensureGradesFound(List<Grade> grades) {
        if (grades.isEmpty()) {
            throw new ResourceNotFoundException("Aucune note trouvee pour ces criteres");
        }
    }

    private void requireUnlockJustification(GradeBatchStatusRequest request) {
        validateBatchRequest(request);
        if (!hasText(request.getJustification())) {
            throw new BadRequestException("Une justification est obligatoire pour deverrouiller des notes.");
        }
    }

    private GradeStatus currentStatus(Grade grade) {
        return grade.getStatus() == null ? GradeStatus.DRAFT : grade.getStatus();
    }
    private void validateRequest(GradeCreateRequestDTO dto, Long currentGradeId) {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Les informations de la note sont obligatoires");
            throw new ValidationException(errors);
        }

        if (dto.getStudentId() == null) {
            errors.add("L'identifiant de l'eleve est obligatoire");
        }
        if (dto.getSubjectId() == null) {
            errors.add("L'identifiant de la matiere est obligatoire");
        }
        if (dto.getClasseId() == null) {
            errors.add("L'identifiant de la classe est obligatoire");
        }
        if (dto.getSequenceId() == null) {
            errors.add("L'identifiant de la sequence est obligatoire");
        }

        if (dto.getScore() == null) {
            errors.add("La note est obligatoire");
        } else if (dto.getScore().compareTo(BigDecimal.ZERO) < 0
                || dto.getScore().compareTo(BigDecimal.valueOf(20)) > 0) {
            errors.add("La note doit etre comprise entre 0 et 20");
        }

        if (dto.getCoefficient() != null && dto.getCoefficient().compareTo(BigDecimal.valueOf(0.1)) < 0) {
            errors.add("Le coefficient doit etre superieur a 0.1");
        }

        if (dto.getComments() != null && dto.getComments().length() > 1000) {
            errors.add("Le commentaire ne doit pas depasser 1000 caracteres");
        }

        if (dto.getStudentId() != null && dto.getSubjectId() != null && dto.getSequenceId() != null) {
            gradeRepository.findFirstByStudentIdAndSubjectIdAndSequenceId(
                    dto.getStudentId(),
                    dto.getSubjectId(),
                    dto.getSequenceId())
                    .filter(existing -> currentGradeId == null || !existing.getId().equals(currentGradeId))
                    .ifPresent(existing -> errors.add(
                            "Une note existe deja pour cet eleve, cette matiere et cette sequence"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void applyRelationsAndValues(Grade grade, GradeCreateRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Eleve non trouve"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Matiere non trouvee"));
        ClasseRoom classe = classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvee"));
        Sequence sequence = sequenceRepository.findById(dto.getSequenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Sequence non trouvee"));

        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setClasse(classe);
        grade.setSequence(sequence);

        if (dto.getTrimestreId() != null) {
            Trimestre trimestre = trimestreRepository.findById(dto.getTrimestreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trimestre non trouve"));
            grade.setTrimestre(trimestre);
        } else if (grade.getId() == null) {
            grade.setTrimestre(null);
        }

        if (grade.getAssessmentDate() == null) {
            grade.setAssessmentDate(LocalDateTime.now());
        }

        if (grade.getCoefficient() == null) {
            grade.setCoefficient(BigDecimal.ONE);
        }

        if (!hasText(grade.getPeriod())) {
            grade.setPeriod(sequence.getLibelleSequence());
        }
    }

    @Transactional(readOnly = true)
    public ClassPerformanceReport generateClassPerformanceReport(Long classId) {
        return generateClassPerformanceReport(classId, null);
    }

    @Transactional(readOnly = true)
    public ClassPerformanceReport generateClassPerformanceReport(Long classId, String period) {
        ClasseRoom classe = classeRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvee"));

        List<Grade> classGrades = gradeRepository.findByClasseId(classId).stream()
                .filter(grade -> periodMatches(grade, period))
                .collect(Collectors.toList());

        ClassPerformanceReport report = new ClassPerformanceReport();
        report.setClassId(classId);
        report.setNameClasse(classe.getNameClasse());
        report.setPeriod(period);

        if (classGrades.isEmpty()) {
            report.setSubjectAverages(new LinkedHashMap<>());
            report.setGradeDistribution(defaultDistribution());
            report.setTotalStudents(0);
            report.setPassingStudents(0);
            report.setClassAverage(0.0);
            report.setSuccessRate(0.0);
            report.setHighestAverage(0.0);
            report.setLowestAverage(0.0);
            return report;
        }

        Map<String, Double> subjectAverages = classGrades.stream()
                .filter(g -> g.getSubject() != null && hasText(g.getSubject().getNameSubject()))
                .collect(Collectors.groupingBy(
                        g -> g.getSubject().getNameSubject(),
                        LinkedHashMap::new,
                        Collectors.averagingDouble(g -> g.getGrade().doubleValue())));

        Map<Long, Double> studentAverages = classGrades.stream()
                .filter(g -> g.getStudent() != null && g.getStudent().getId() != null)
                .collect(Collectors.groupingBy(
                        g -> g.getStudent().getId(),
                        Collectors.averagingDouble(g -> g.getGrade().doubleValue())));

        int totalStudents = studentAverages.size();
        int passingStudents = (int) studentAverages.values().stream()
                .filter(avg -> avg >= 10.0)
                .count();

        report.setSubjectAverages(subjectAverages);
        report.setClassAverage(classGrades.stream()
                .map(Grade::getGrade)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0));
        report.setTotalStudents(totalStudents);
        report.setPassingStudents(passingStudents);
        report.setSuccessRate(totalStudents == 0 ? 0.0 : (passingStudents * 100.0) / totalStudents);
        report.setHighestAverage(studentAverages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        report.setLowestAverage(studentAverages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        report.setGradeDistribution(buildDistribution(classGrades));

        return report;
    }

    @CacheEvict(value = "grades", allEntries = true)
    public void clearGradesCache() {
        log.debug("Vidage du cache des notes");
    }

    private boolean periodMatches(Grade grade, String period) {
        if (!hasText(period)) {
            return true;
        }

        String normalizedPeriod = period.trim();
        if (hasText(grade.getPeriod()) && grade.getPeriod().equalsIgnoreCase(normalizedPeriod)) {
            return true;
        }
        return grade.getSequence() != null
                && hasText(grade.getSequence().getLibelleSequence())
                && grade.getSequence().getLibelleSequence().equalsIgnoreCase(normalizedPeriod);
    }

    private List<Map.Entry<Long, Double>> rankedStudents(List<Grade> grades) {
        return grades.stream()
                .filter(grade -> grade.getStudent() != null && grade.getStudent().getId() != null)
                .collect(Collectors.groupingBy(
                        grade -> grade.getStudent().getId(),
                        Collectors.averagingDouble(grade -> grade.getGrade() == null ? 0.0 : grade.getGrade().doubleValue())))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> subjectAverageRow(List<Grade> subjectGrades) {
        Grade first = subjectGrades.get(0);
        double average = average(subjectGrades);
        double coefficient = first.getCoefficient() != null
                ? first.getCoefficient().doubleValue()
                : first.getSubject() != null && first.getSubject().getCoefficient() != null
                        ? first.getSubject().getCoefficient().doubleValue()
                        : 1.0;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("subjectId", first.getSubject().getId());
        row.put("subjectName", first.getSubject().getNameSubject());
        row.put("average", average);
        row.put("coefficient", coefficient);
        row.put("weightedAverage", average * coefficient);
        return row;
    }

    private double average(List<Grade> grades) {
        return grades.stream()
                .map(Grade::getGrade)
                .filter(value -> value != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double max(List<Grade> grades) {
        return grades.stream()
                .map(Grade::getGrade)
                .filter(value -> value != null)
                .mapToDouble(BigDecimal::doubleValue)
                .max()
                .orElse(0.0);
    }

    private double min(List<Grade> grades) {
        return grades.stream()
                .map(Grade::getGrade)
                .filter(value -> value != null)
                .mapToDouble(BigDecimal::doubleValue)
                .min()
                .orElse(0.0);
    }

    private double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }

    private String buildStudentName(Student student) {
        String lastName = student.getLastNameStudent() == null ? "" : student.getLastNameStudent();
        String firstName = student.getFirstNameStudent() == null ? "" : student.getFirstNameStudent();
        return (lastName + " " + firstName).trim();
    }

    private String teacherComment(double average) {
        if (average >= 14.0) {
            return "Tres bon travail.";
        }
        if (average >= 10.0) {
            return "Resultats satisfaisants.";
        }
        return "Des efforts supplementaires sont necessaires.";
    }

    private Map<String, Integer> buildDistribution(List<Grade> grades) {
        Map<String, Integer> distribution = defaultDistribution();
        for (Grade grade : grades) {
            String key = bucketFor(grade.getGrade());
            distribution.put(key, distribution.getOrDefault(key, 0) + 1);
        }
        return distribution;
    }

    private Map<String, Integer> defaultDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-4.99", 0);
        distribution.put("5-9.99", 0);
        distribution.put("10-13.99", 0);
        distribution.put("14-16.99", 0);
        distribution.put("17-20", 0);
        return distribution;
    }

    private String bucketFor(BigDecimal value) {
        double grade = value != null ? value.doubleValue() : 0.0;
        if (grade < 5) {
            return "0-4.99";
        }
        if (grade < 10) {
            return "5-9.99";
        }
        if (grade < 14) {
            return "10-13.99";
        }
        if (grade < 17) {
            return "14-16.99";
        }
        return "17-20";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}



