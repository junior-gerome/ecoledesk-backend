package com.school.platform.academic.application.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.academic.application.dto.affectation.AffectationCreateRequest;
import com.school.platform.academic.application.dto.affectation.AffectationDTO;
import com.school.platform.academic.application.dto.affectation.AffectationResponse;
import com.school.platform.academic.application.dto.affectation.AffectationUpdateRequest;
import com.school.platform.academic.application.interfaces.AffectationService;
import com.school.platform.academic.domain.model.Affectation;
import com.school.platform.academic.infrastructure.persistence.AffectationRepository;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.academic.infrastructure.persistence.SubjectRepository;
import com.school.platform.staff.infrastructure.persistence.StaffMemberRepository;
import com.school.platform.academic.infrastructure.persistence.TypeAffectationRepository;
import com.school.platform.shared.application.BusinessAuditService;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffectationServiceImpl implements AffectationService {
    private final AffectationRepository affectationRepository;
    private final StaffMemberRepository enseignantRepository;
    private final ClasseRoomRepository classeRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TypeAffectationRepository typeAffectationRepository;
    private final GradeRepository gradeRepository;
    private final BusinessAuditService businessAuditService;

    @Transactional(readOnly = true)
    public List<AffectationDTO> getAllAffectations() {
        return affectationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> getAllAffectationResponses() {
        return affectationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AffectationResponse getAffectationById(Long id) {
        return affectationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", "id", id));
    }

    @Transactional(readOnly = true)
    public List<AffectationDTO> getAffectationsByEnseignant(Long enseignantId) {
        return affectationRepository.findByTeacherId(enseignantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> getAffectationResponsesByTeacher(Long teacherId) {
        return affectationRepository.findByTeacherId(teacherId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationDTO> getAffectationsByClasse(Long classeId) {
        return affectationRepository.findByClasseId(classeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> getAffectationResponsesByClasse(Long classeId) {
        return affectationRepository.findByClasseId(classeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationResponse> getAffectationResponsesBySchoolYear(Long schoolYearId) {
        return affectationRepository.findByAcademicYearId(schoolYearId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AffectationDTO createAffectation(AffectationDTO dto) {
        validateAffectation(dto, null);
        Affectation affectation = new Affectation();
        updateAffectationFromDTO(affectation, dto);
        Affectation saved = affectationRepository.save(affectation);
        businessAuditService.record("AFFECTATION_CREATED", "affectation", saved.getId());
        return convertToDTO(saved);
    }

    @Transactional
    public AffectationResponse createAffectation(AffectationCreateRequest request) {
        return toResponseFromDto(createAffectation(toDto(request)));
    }

    @Transactional
    public AffectationDTO updateAffectation(Long id, AffectationDTO dto) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", "id", id));

        validateAffectation(dto, id);
        updateAffectationFromDTO(affectation, dto);
        Affectation saved = affectationRepository.save(affectation);
        businessAuditService.record("AFFECTATION_UPDATED", "affectation", saved.getId());
        return convertToDTO(saved);
    }

    @Transactional
    public AffectationResponse updateAffectation(Long id, AffectationUpdateRequest request) {
        return toResponseFromDto(updateAffectation(id, toDto(request)));
    }

    @Transactional
    public void deleteAffectation(Long id) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", "id", id));

        if (gradeRepository.existsByClasseIdAndSubjectId(
                affectation.getClasse().getId(), affectation.getSubject().getId())) {
            throw new BadRequestException("Impossible de supprimer une affectation utilisee par des notes.");
        }

        affectationRepository.deleteById(id);
        businessAuditService.record("AFFECTATION_DELETED", "affectation", id);
    }

    private void validateAffectation(AffectationDTO dto, Long currentId) {
        if (dto == null) {
            throw new BadRequestException("Les informations de l'affectation sont requises.");
        }

        requireId(dto.getTeachertId(), "L'enseignant est requis.");
        requireId(dto.getClasseId(), "La classe est requise.");
        requireId(dto.getSubjectId(), "La matiere est requise.");
        requireId(dto.getAcademicYearId(), "L'annee scolaire est requise.");

        if (dto.getDateDebut() != null && dto.getDateFin() != null
                && dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new BadRequestException("La date de fin doit etre posterieure a la date de debut");
        }

        affectationRepository.findByTeacherIdAndClasseIdAndSubjectIdAndAcademicYearId(
                dto.getTeachertId(), dto.getClasseId(), dto.getSubjectId(), dto.getAcademicYearId())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new BadRequestException("Cette affectation existe deja");
                });

        if (dto.getTypeCode() == null || dto.getTypeCode().isBlank()
                || !typeAffectationRepository.existsById(dto.getTypeCode())) {
            throw new BadRequestException("Type d'affectation invalide");
        }
    }

    private void updateAffectationFromDTO(Affectation affectation, AffectationDTO dto) {
        affectation.setTypeCode(dto.getTypeCode());
        affectation.setDateDebut(dto.getDateDebut());
        affectation.setDateFin(dto.getDateFin());

        affectation.setTeacher(enseignantRepository.findById(dto.getTeachertId())
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", "id", dto.getTeachertId())));

        affectation.setClasse(classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe", "id", dto.getClasseId())));

        affectation.setSubject(subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Matiere", "id", dto.getSubjectId())));

        affectation.setAcademicYear(academicYearRepository.findById(dto.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", dto.getAcademicYearId())));
    }

    private AffectationDTO convertToDTO(Affectation affectation) {
        return AffectationDTO.builder()
                .id(affectation.getId())
                .typeCode(affectation.getTypeCode())
                .dateDebut(affectation.getDateDebut())
                .dateFin(affectation.getDateFin())
                .teachertId(affectation.getTeacher().getId())
                .classeId(affectation.getClasse().getId())
                .subjectId(affectation.getSubject().getId())
                .academicYearId(affectation.getAcademicYear().getId())
                .LastnameTeacher(affectation.getTeacher().getLastnameTeacher() + " "
                        + affectation.getTeacher().getFirstnameTeacher())
                .NameClasse(affectation.getClasse().getNameClasse())
                .nameSubject(affectation.getSubject().getNameSubject())
                .libelleAcademicYear(affectation.getAcademicYear().getLibelleAcademicYear())
                .build();
    }

    private AffectationDTO toDto(AffectationCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Les informations de l'affectation sont requises.");
        }
        return AffectationDTO.builder()
                .typeCode(request.getTypeCode())
                .teachertId(request.getTeacherId())
                .classeId(request.getClasseId())
                .subjectId(request.getSubjectId())
                .academicYearId(request.getAcademicYearId())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .build();
    }

    private AffectationResponse toResponseFromDto(AffectationDTO dto) {
        Affectation affectation = affectationRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", "id", dto.getId()));
        return toResponse(affectation);
    }

    private AffectationResponse toResponse(Affectation affectation) {
        return AffectationResponse.builder()
                .id(affectation.getId())
                .typeCode(affectation.getTypeCode())
                .dateDebut(affectation.getDateDebut())
                .dateFin(affectation.getDateFin())
                .teacherId(affectation.getTeacher().getId())
                .classeId(affectation.getClasse().getId())
                .subjectId(affectation.getSubject().getId())
                .academicYearId(affectation.getAcademicYear().getId())
                .teacherName((safe(affectation.getTeacher().getLastnameTeacher()) + " "
                        + safe(affectation.getTeacher().getFirstnameTeacher())).trim())
                .classeName(affectation.getClasse().getNameClasse())
                .subjectName(affectation.getSubject().getNameSubject())
                .libelleAcademicYear(affectation.getAcademicYear().getLibelleAcademicYear())
                .build();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void requireId(Long value, String message) {
        if (value == null || value <= 0) {
            throw new BadRequestException(message);
        }
    }
}
