package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.management.dto.AffectationDTO;
import com.school.management.model.Affectation;
import com.school.management.repository.AffectationRepository;
import com.school.management.repository.AnneeScolaireRepository;
import com.school.management.repository.ClasseRoomRepository;
import com.school.management.repository.TeacherRepository;
import com.school.management.repository.SubjectRepository;
import com.school.management.repository.TypeAffectationRepository;

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
        validateAffectation(dto);
        Affectation affectation = new Affectation();
        updateAffectationFromDTO(affectation, dto);
        return convertToDTO(affectationRepository.save(affectation));
    }

    @Transactional
    public AffectationDTO updateAffectation(Long id, AffectationDTO dto) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation non trouvée"));

        validateAffectation(dto);
        updateAffectationFromDTO(affectation, dto);
        return convertToDTO(affectationRepository.save(affectation));
    }

    @Transactional
    public void deleteAffectation(Long id) {
        if (!affectationRepository.existsById(id)) {
            throw new RuntimeException("Affectation non trouvée");
        }
        affectationRepository.deleteById(id);
    }

    private void validateAffectation(AffectationDTO dto) {
        if (dto.getDateDebut() != null && dto.getDateFin() != null &&
            dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new RuntimeException("La date de fin doit être postérieure à la date de début");
        }

        if (affectationRepository.existsByTeacherIdAndClasseIdAndSubjectIdAndAnneeScolaireId(
                dto.getId(), dto.getId(), dto.getId(), dto.getId())) {
            throw new RuntimeException("Cette affectation existe déjà");
        }

        if (!typeAffectationRepository.existsById(dto.getTypeCode())) {
            throw new RuntimeException("Type d'affectation invalide");
        }
    }

    private void updateAffectationFromDTO(Affectation affectation, AffectationDTO dto) {
        affectation.setTypeCode(dto.getTypeCode());
        affectation.setDateDebut(dto.getDateDebut());
        affectation.setDateFin(dto.getDateFin());

        affectation.setTeacher(enseignantRepository.findById(dto.getTeachertId())
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé")));

        affectation.setClasse(classeRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Classe non trouvée")));

        affectation.setSubject(subjectRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Matière non trouvée")));

        affectation.setAnneeScolaire(anneeScolaireRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée")));
    }

    private AffectationDTO convertToDTO(Affectation affectation) {
        return AffectationDTO.builder()
                .id(affectation.getId())
                .typeCode(affectation.getTypeCode())
                .dateDebut(affectation.getDateDebut())
                .dateFin(affectation.getDateFin())
                .teachertId(affectation.getTeacher().getId())
                .LastnameTeacher(affectation.getTeacher().getLastnameTeacher() + " " + affectation.getTeacher().getFirstnameTeacher())
                .NameClasse(affectation.getClasse().getNameClasse())
                .nameSubject(affectation.getSubject().getNameSubject())
                .libelleAnneeScolaire(affectation.getAnneeScolaire().getLibelleAnneeScolaire())
                .build();
       
    }
}
