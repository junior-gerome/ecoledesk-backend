package com.school.platform.academic.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.AffectationDTO;
import com.school.platform.academic.domain.model.Affectation;
import com.school.platform.academic.infrastructure.persistence.AffectationRepository;
import com.school.platform.academic.infrastructure.persistence.AnneeScolaireRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.SubjectRepository;
import com.school.platform.academic.infrastructure.persistence.TeacherRepository;
import com.school.platform.academic.infrastructure.persistence.TypeAffectationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffectationService {
    private final AffectationRepository affectationRepository;
    private final TeacherRepository enseignantRepository;
    private final ClasseRoomRepository classeRepository;
    private final SubjectRepository subjectRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final TypeAffectationRepository typeAffectationRepository;

    @Transactional(readOnly = true)
    public List<AffectationDTO> getAllAffectations() {
        return affectationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationDTO> getAffectationsByEnseignant(Long enseignantId) {
        return affectationRepository.findByTeacherId(enseignantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AffectationDTO> getAffectationsByClasse(Long classeId) {
        return affectationRepository.findByClasseId(classeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AffectationDTO createAffectation(AffectationDTO dto) {
        validateAffectation(dto, null);
        Affectation affectation = new Affectation();
        updateAffectationFromDTO(affectation, dto);
        return convertToDTO(affectationRepository.save(affectation));
    }

    @Transactional
    public AffectationDTO updateAffectation(Long id, AffectationDTO dto) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", "id", id));

        validateAffectation(dto, id);
        updateAffectationFromDTO(affectation, dto);
        return convertToDTO(affectationRepository.save(affectation));
    }

    @Transactional
    public void deleteAffectation(Long id) {
        if (!affectationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Affectation", "id", id);
        }
        affectationRepository.deleteById(id);
    }

    private void validateAffectation(AffectationDTO dto, Long currentId) {
        if (dto == null) {
            throw new BadRequestException("Les informations de l'affectation sont requises.");
        }

        requireId(dto.getTeachertId(), "L'enseignant est requis.");
        requireId(dto.getClasseId(), "La classe est requise.");
        requireId(dto.getSubjectId(), "La matiere est requise.");
        requireId(dto.getAnneeScolaireId(), "L'annee scolaire est requise.");

        if (dto.getDateDebut() != null && dto.getDateFin() != null
                && dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new BadRequestException("La date de fin doit etre posterieure a la date de debut");
        }

        affectationRepository.findByTeacherIdAndClasseIdAndSubjectIdAndAnneeScolaireId(
                dto.getTeachertId(), dto.getClasseId(), dto.getSubjectId(), dto.getAnneeScolaireId())
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

        affectation.setAnneeScolaire(anneeScolaireRepository.findById(dto.getAnneeScolaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Annee scolaire", "id", dto.getAnneeScolaireId())));
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
                .anneeScolaireId(affectation.getAnneeScolaire().getId())
                .LastnameTeacher(affectation.getTeacher().getLastnameTeacher() + " "
                        + affectation.getTeacher().getFirstnameTeacher())
                .NameClasse(affectation.getClasse().getNameClasse())
                .nameSubject(affectation.getSubject().getNameSubject())
                .libelleAnneeScolaire(affectation.getAnneeScolaire().getLibelleAnneeScolaire())
                .build();
    }

    private void requireId(Long value, String message) {
        if (value == null || value <= 0) {
            throw new BadRequestException(message);
        }
    }
}
