package com.school.platform.enrollment.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.ClasseRoomStudentCountDTO;
import com.school.platform.enrollment.application.dto.InscriptionStudentDTO;
import com.school.platform.enrollment.application.dto.PreinscriptionStatusResponse;
import com.school.platform.academic.application.dto.SectionStudentCountDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.mapper.InscriptionStudentMapper;
import com.school.platform.academic.domain.model.AnneeScolaire;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.enrollment.domain.model.InscriptionStudent;
import com.school.platform.enrollment.domain.model.PreinscriptionStatus;
import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.billing.domain.model.Montant;
import com.school.platform.academic.domain.model.Section;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.academic.infrastructure.persistence.AnneeScolaireRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.billing.infrastructure.persistence.MontantRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;
import com.school.platform.academic.infrastructure.persistence.SectionRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.shared.application.BusinessAuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InscriptionStudentService {

    private final InscriptionStudentRepository inscriptionStudentRepository;
    private final StudentRepository studentRepository;
    private final ClasseRoomRepository classeRoomRepository;
    private final MontantRepository montantRepository;
    private final PaiementRepository paiementRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final SectionRepository sectionRepository;
    private final StudentService studentService;
    private final BusinessAuditService businessAuditService;
    private final InscriptionStudentMapper mapper;

    @Value("${school.preinscription.validation.requires-payment:false}")
    private boolean validationRequiresPreinscriptionPayment;

    public InscriptionStudentDTO createPreinscription(InscriptionStudentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les informations de la preinscription sont requises.");
        }

        dto.setStatutPreinscription(PreinscriptionStatus.EN_ATTENTE);
        if (dto.getDatePreinscription() == null) {
            dto.setDatePreinscription(LocalDate.now());
        }
        if (dto.getDateInscription() == null) {
            dto.setDateInscription(dto.getDatePreinscription());
        }

        InscriptionStudentDTO created = createInscription(dto);
        businessAuditService.record("PREINSCRIPTION_CREATED", "inscription_student", created.getId());
        return created;
    }

    public InscriptionStudentDTO createInscription(InscriptionStudentDTO dto) {
        if (dto == null || dto.getStudent() == null) {
            throw new IllegalArgumentException("Les informations de l'inscription et de l'eleve sont requises.");
        }
        requireId(dto.getClasseRoomId(), "La classe est requise.");
        requireId(dto.getMontantId(), "Les frais de preinscription sont requis.");
        requireId(dto.getAnneeScolaireId(), "L'annee scolaire est requise.");

        ClasseRoom selectedClasse = classeRoomRepository.findByIdForUpdate(dto.getClasseRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe", "id", dto.getClasseRoomId()));
        Montant selectedMontant = montantRepository.findById(dto.getMontantId())
                .orElseThrow(() -> new ResourceNotFoundException("Montant", "id", dto.getMontantId()));
        AnneeScolaire selectedAnnee = anneeScolaireRepository.findById(dto.getAnneeScolaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Annee scolaire", "id", dto.getAnneeScolaireId()));

        validateSectionConsistency(dto.getSectionId(), selectedClasse);
        validateClassCapacity(selectedClasse, selectedAnnee.getId());
        validateNoDuplicateEnrollment(dto.getStudent(), selectedAnnee.getId());

        StudentDTO savedStudentDTO = studentService.createStudentWithParent(dto.getStudent());
        Student studentEntity = studentRepository.findById(savedStudentDTO.getId())
                .orElseThrow(() -> new IllegalStateException("Student not found after creation"));
        validateStudentNotAlreadyEnrolled(studentEntity.getId(), selectedAnnee.getId());

        InscriptionStudent entity = mapper.toEntity(dto);
        entity.setStudent(studentEntity);
        entity.setClasseRoom(selectedClasse);
        entity.setMontant(selectedMontant);
        entity.setAnneeScolaire(selectedAnnee);

        InscriptionStudent saved = inscriptionStudentRepository.save(entity);
        return mapper.toDto(saved);
    }

    public InscriptionStudentDTO updateInscription(Long id, InscriptionStudentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les informations de l'inscription sont requises.");
        }

        InscriptionStudent existingInscription = inscriptionStudentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvee avec l'ID : " + id));

        if (dto.getStudent() != null) {
            Long studentId = dto.getStudent().getId();
            if (studentId == null && existingInscription.getStudent() != null) {
                studentId = existingInscription.getStudent().getId();
            }
            if (studentId != null) {
                studentService.updateStudent(studentId, dto.getStudent());
            }
        }

        ClasseRoom selectedClasse = existingInscription.getClasseRoom();
        if (dto.getClasseRoomId() != null) {
            selectedClasse = classeRoomRepository.findById(dto.getClasseRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvee"));
            existingInscription.setClasseRoom(selectedClasse);
        }

        validateSectionConsistency(dto.getSectionId(), selectedClasse);

        if (dto.getMontantId() != null) {
            Montant montant = montantRepository.findById(dto.getMontantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Montant non trouve"));
            existingInscription.setMontant(montant);
        }

        if (dto.getAnneeScolaireId() != null) {
            AnneeScolaire annee = anneeScolaireRepository.findById(dto.getAnneeScolaireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Annee scolaire non trouvee"));
            existingInscription.setAnneeScolaire(annee);
        }

        mapper.updateEntityFromDto(dto, existingInscription);

        InscriptionStudent updated = inscriptionStudentRepository.save(existingInscription);
        log.info("Mise a jour complete reussie pour l'inscription ID : {}", updated.getId());
        return mapper.toDto(updated);
    }

    public List<InscriptionStudentDTO> getAll() {
        return inscriptionStudentRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<InscriptionStudentDTO> getByClass(Long classeRoomId, Long anneeScolaireId) {
        if (classeRoomId == null) {
            throw new IllegalArgumentException("L'identifiant de la classe est requis.");
        }

        classeRoomRepository.findById(classeRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvee avec l'ID : " + classeRoomId));

        Long resolvedSchoolYearId = anneeScolaireId;
        if (resolvedSchoolYearId == null) {
            resolvedSchoolYearId = anneeScolaireRepository.findByStatutCode(true)
                    .map(AnneeScolaire::getId)
                    .orElse(null);
        }

        List<InscriptionStudent> inscriptions = resolvedSchoolYearId != null
                ? inscriptionStudentRepository.findByClasseRoomIdAndAnneeScolaireId(classeRoomId, resolvedSchoolYearId)
                : inscriptionStudentRepository.findByClasseRoomId(classeRoomId);

        if (inscriptions.isEmpty() && resolvedSchoolYearId != null) {
            inscriptions = inscriptionStudentRepository.findByClasseRoomId(classeRoomId);
        }

        return inscriptions.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public InscriptionStudentDTO getById(Long id) {
        return inscriptionStudentRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvee avec l'ID : " + id));
    }

    public InscriptionStudentDTO getByStudentAndClass(Long studentId, Long classeRoomId) {
        return inscriptionStudentRepository.findByStudentIdAndClasseRoomId(studentId, classeRoomId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscription non trouvee pour l'etudiant avec l'ID : "
                                + studentId + " et la classe avec l'ID : " + classeRoomId));
    }

    public long getCountByClass(Long classeRoomId) {
        return inscriptionStudentRepository.countByClasseRoomId(classeRoomId);
    }

    public void delete(Long id) {
        if (!inscriptionStudentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inscription non trouvee avec l'ID : " + id);
        }
        inscriptionStudentRepository.deleteById(id);
    }

    public List<SectionStudentCountDTO> getStudentCountBySection() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        return sectionRepository.findAll().stream()
                .map(section -> {
                    List<ClasseRoom> classes = classeRoomRepository.findBySection(section);
                    long totalStudents = classes.stream()
                            .mapToLong(classe -> inscriptionStudentRepository.countByClasseRoom(classe))
                            .sum();
                    long newStudents = classes.stream()
                            .flatMap(classe -> inscriptionStudentRepository.findByClasseRoom(classe).stream())
                            .filter(inscription -> inscription.getDateInscription() != null
                                    && !inscription.getDateInscription().isBefore(monthStart))
                            .map(InscriptionStudent::getStudent)
                            .filter(student -> student != null && student.getId() != null)
                            .map(Student::getId)
                            .distinct()
                            .count();
                    return new SectionStudentCountDTO(section.getLibelle(), totalStudents, newStudents);
                })
                .collect(Collectors.toList());
    }

    public List<ClasseRoomStudentCountDTO> getStudentCountByClasse() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        return classeRoomRepository.findAll().stream()
                .map(classe -> {
                    long totalStudents = inscriptionStudentRepository.countByClasseRoomId(classe.getId());
                    long newStudents = inscriptionStudentRepository.findByClasseRoom(classe).stream()
                            .filter(inscription -> inscription.getDateInscription() != null
                                    && !inscription.getDateInscription().isBefore(monthStart))
                            .map(InscriptionStudent::getStudent)
                            .filter(student -> student != null && student.getId() != null)
                            .map(Student::getId)
                            .distinct()
                            .count();
                    return new ClasseRoomStudentCountDTO(classe.getNameClasse(), totalStudents, newStudents);
                })
                .collect(Collectors.toList());
    }

    public PreinscriptionStatusResponse validatePreinscription(Long id) {
        InscriptionStudent inscription = findPreinscriptionForDecision(id);
        ensureDecisionAllowed(inscription, PreinscriptionStatus.VALIDEE);
        if (validationRequiresPreinscriptionPayment
                && !paiementRepository.existsByInscriptionStudentIdAndTypePaiementAndCancelledAtIsNull(
                        id, TypePaiement.FRAIS_PREINSCRIPTION)) {
            throw new BadRequestException("Un paiement de preinscription est obligatoire avant validation.");
        }

        applyPreinscriptionDecision(inscription, PreinscriptionStatus.VALIDEE, null);
        InscriptionStudent saved = inscriptionStudentRepository.save(inscription);
        businessAuditService.record("PREINSCRIPTION_VALIDATED", "inscription_student", saved.getId());
        businessAuditService.record("INSCRIPTION_CONFIRMED", "inscription_student", saved.getId());
        return toStatusResponse(saved);
    }

    public PreinscriptionStatusResponse rejectPreinscription(Long id, String justification) {
        InscriptionStudent inscription = findPreinscriptionForDecision(id);
        ensureDecisionAllowed(inscription, PreinscriptionStatus.REFUSEE);
        String reason = requireDecisionReason(justification);
        applyPreinscriptionDecision(inscription, PreinscriptionStatus.REFUSEE, reason);
        InscriptionStudent saved = inscriptionStudentRepository.save(inscription);
        businessAuditService.record("PREINSCRIPTION_REJECTED", "inscription_student", saved.getId());
        return toStatusResponse(saved);
    }

    public PreinscriptionStatusResponse cancelPreinscription(Long id, String justification) {
        InscriptionStudent inscription = findPreinscriptionForDecision(id);
        ensureDecisionAllowed(inscription, PreinscriptionStatus.ANNULEE);
        String reason = requireDecisionReason(justification);
        applyPreinscriptionDecision(inscription, PreinscriptionStatus.ANNULEE, reason);
        InscriptionStudent saved = inscriptionStudentRepository.save(inscription);
        businessAuditService.record("PREINSCRIPTION_CANCELLED", "inscription_student", saved.getId());
        return toStatusResponse(saved);
    }

    private InscriptionStudent findPreinscriptionForDecision(Long id) {
        return inscriptionStudentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preinscription", "id", id));
    }

    private void ensureDecisionAllowed(InscriptionStudent inscription, PreinscriptionStatus targetStatus) {
        PreinscriptionStatus currentStatus = inscription.getStatutPreinscription();
        if (currentStatus == null) {
            currentStatus = PreinscriptionStatus.INSCRITE;
        }
        if (currentStatus != PreinscriptionStatus.EN_ATTENTE && currentStatus != PreinscriptionStatus.BROUILLON) {
            throw new BadRequestException("Transition de preinscription interdite depuis le statut " + currentStatus + ".");
        }
        if (targetStatus == PreinscriptionStatus.VALIDEE
                && (inscription.getStudent() == null || inscription.getClasseRoom() == null || inscription.getAnneeScolaire() == null)) {
            throw new BadRequestException("La preinscription ne contient pas toutes les informations necessaires a la validation.");
        }
    }

    private void applyPreinscriptionDecision(InscriptionStudent inscription, PreinscriptionStatus status, String reason) {
        inscription.setStatutPreinscription(status);
        inscription.setPreinscriptionDecisionReason(reason);
        inscription.setPreinscriptionDecisionAt(LocalDateTime.now());
        inscription.setPreinscriptionDecisionBy(businessAuditService.currentUserId().orElse(null));
    }

    private String requireDecisionReason(String justification) {
        if (justification == null || justification.isBlank()) {
            throw new BadRequestException("Une justification est obligatoire pour cette decision.");
        }
        String reason = justification.trim();
        if (reason.length() > 500) {
            throw new BadRequestException("La justification ne doit pas depasser 500 caracteres.");
        }
        return reason;
    }

    private PreinscriptionStatusResponse toStatusResponse(InscriptionStudent inscription) {
        return PreinscriptionStatusResponse.builder()
                .id(inscription.getId())
                .studentId(inscription.getStudent() == null ? null : inscription.getStudent().getId())
                .status(inscription.getStatutPreinscription())
                .reason(inscription.getPreinscriptionDecisionReason())
                .changedBy(inscription.getPreinscriptionDecisionBy())
                .changedAt(inscription.getPreinscriptionDecisionAt())
                .build();
    }
    private void validateSectionConsistency(Long requestedSectionId, ClasseRoom selectedClasse) {
        if (requestedSectionId == null) {
            return;
        }

        Section section = sectionRepository.findById(requestedSectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section non trouvee"));

        if (selectedClasse == null || selectedClasse.getSection() == null
                || !selectedClasse.getSection().getId().equals(section.getId())) {
            throw new IllegalArgumentException("La classe selectionnee n'appartient pas a la section choisie.");
        }
    }

    private void validateClassCapacity(ClasseRoom selectedClasse, Long schoolYearId) {
        if (selectedClasse.getCapacity() == null || selectedClasse.getCapacity() <= 0) {
            return;
        }

        long currentCount = inscriptionStudentRepository.countByClasseRoomIdAndAnneeScolaireIdAndStatutPreinscriptionIn(
                selectedClasse.getId(), schoolYearId, activePreinscriptionStatuses());
        if (currentCount >= selectedClasse.getCapacity()) {
            throw new BadRequestException("La classe selectionnee a deja atteint sa capacite maximale.");
        }
    }

    private void validateNoDuplicateEnrollment(StudentDTO student, Long schoolYearId) {
        if (student.getId() != null) {
            validateStudentNotAlreadyEnrolled(student.getId(), schoolYearId);
            return;
        }

        List<Student> possibleDuplicates = studentRepository
                .findByLastNameStudentIgnoreCaseAndFirstNameStudentIgnoreCaseAndDateOfBirth(
                        student.getLastNameStudent(),
                        student.getFirstNameStudent(),
                        student.getDateOfBirth());

        for (Student duplicate : possibleDuplicates) {
            if (duplicate.getId() != null
                    && inscriptionStudentRepository.existsByStudentIdAndAnneeScolaireIdAndStatutPreinscriptionIn(
                            duplicate.getId(), schoolYearId, activePreinscriptionStatuses())) {
                throw new BadRequestException(
                        "Un eleve avec les memes informations est deja inscrit ou preinscrit pour cette annee scolaire.");
            }
        }
    }

    private void validateStudentNotAlreadyEnrolled(Long studentId, Long schoolYearId) {
        if (studentId != null
                && inscriptionStudentRepository.existsByStudentIdAndAnneeScolaireIdAndStatutPreinscriptionIn(
                        studentId, schoolYearId, activePreinscriptionStatuses())) {
            throw new BadRequestException("Cet eleve est deja inscrit ou preinscrit pour cette annee scolaire.");
        }
    }


    private List<PreinscriptionStatus> activePreinscriptionStatuses() {
        return List.of(
                PreinscriptionStatus.BROUILLON,
                PreinscriptionStatus.EN_ATTENTE,
                PreinscriptionStatus.VALIDEE,
                PreinscriptionStatus.INSCRITE);
    }
    private void requireId(Long value, String message) {
        if (value == null || value <= 0) {
            throw new BadRequestException(message);
        }
    }
}
