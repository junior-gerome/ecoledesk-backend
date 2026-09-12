package com.school.platform.mockdata.infrastructure.bootstrap;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Section;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.SectionRepository;
import com.school.platform.billing.application.PreEnrollmentFeePaymentService;
import com.school.platform.billing.application.dto.RecordPreEnrollmentFeePaymentRequest;
import com.school.platform.billing.domain.model.Montant;
import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.billing.infrastructure.persistence.MontantRepository;
import com.school.platform.enrollment.application.StudentCommandService;
import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentDocumentRequest;
import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentGuardianRequest;
import com.school.platform.enrollment.application.dto.preenrollment.CreatePreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentResponse;
import com.school.platform.enrollment.application.preenrollment.PreEnrollmentCommandService;
import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.model.RelationshipType;
import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seed des données de démonstration en profil dev uniquement.
 * Centralise dans un module dédié les données mock qui alimentent
 * directement la base et remontent dans le frontend.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@Profile("dev")
public class MockDataBootstrap implements ApplicationRunner {

    private final AcademicYearRepository academicYearRepository;
    private final SectionRepository sectionRepository;
    private final ClasseRoomRepository classeRoomRepository;
    private final MontantRepository montantRepository;
    private final StudentRepository studentRepository;
    private final StudentCommandService studentCommandService;
    private final PreEnrollmentRepository preEnrollmentRepository;
    private final PreEnrollmentCommandService preEnrollmentCommandService;
    private final PreEnrollmentFeePaymentService preEnrollmentFeePaymentService;

    @Value("${app.bootstrap.mockdata.enabled:true}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        log.info("=== MockDataBootstrap ===");
        if (!enabled) {
            log.info("Mock data désactivé.");
            return;
        }
        seedAcademicYear();
        seedSectionsAndClasses();
        seedMontants();
        seedStudents();
        seedPreEnrollments();
        log.info("Mock data prêt.");
    }

    private void seedAcademicYear() {
        Map<String, boolean[]> annees = new LinkedHashMap<>();
        annees.put("2026-2027", new boolean[]{true, true});
        annees.put("2025-2026", new boolean[]{false, false});
        annees.forEach((libelle, proprietes) -> {
            if (academicYearRepository.existsByLibelleAcademicYear(libelle)) {
                return;
            }
            AcademicYear annee = new AcademicYear();
            annee.setLibelleAcademicYear(libelle);
            annee.setDateDebut(LocalDate.of(proprietes[0] ? 2026 : 2025, 9, 1));
            annee.setDateFin(LocalDate.of(proprietes[1] ? 2027 : 2026, 8, 31));
            annee.setStatutCode(proprietes[0]);
            academicYearRepository.save(annee);
            log.info("Année scolaire créée : {}", libelle);
        });
    }

    private void seedSectionsAndClasses() {
        Map<String, List<Object[]>> sections = new LinkedHashMap<>();
        sections.put("Maternelle", List.of(
                new Object[]{"PS", "Petite Section", 25},
                new Object[]{"MS", "Moyenne Section", 25},
                new Object[]{"GS", "Grande Section", 25}));
        sections.put("Primaire", List.of(
                new Object[]{"CP", "Cours Préparatoire", 30},
                new Object[]{"CE1", "Cours Elémentaire 1", 30},
                new Object[]{"CE2", "Cours Elémentaire 2", 30},
                new Object[]{"CM1", "Cours Moyen 1", 30},
                new Object[]{"CM2", "Cours Moyen 2", 30}));
        sections.put("Secondaire", List.of(
                new Object[]{"6eme", "Sixième", 35},
                new Object[]{"5eme", "Cinquième", 35},
                new Object[]{"4eme", "Quatrième", 35},
                new Object[]{"3eme", "Troisième", 35},
                new Object[]{"2nde", "Seconde", 35},
                new Object[]{"1re", "Première", 35},
                new Object[]{"Tle", "Terminale", 35}));

        AcademicYear active = academicYearRepository.findByStatutCode(true)
                .orElseThrow(() -> new IllegalStateException("Aucune année scolaire active pour le mock data."));

        sections.forEach((sectionLibelle, classes) -> {
            Section section = sectionRepository.findByLibelle(sectionLibelle)
                    .orElseGet(() -> {
                        Section nouvelle = new Section();
                        nouvelle.setLibelle(sectionLibelle);
                        nouvelle.setDescription("Section " + sectionLibelle);
                        return sectionRepository.save(nouvelle);
                    });
            for (Object[] classe : classes) {
                String nameClasse = (String) classe[0];
                String level = (String) classe[1];
                Integer capacite = (Integer) classe[2];
                if (classeRoomRepository.existsByNameClasseAndSectionIdAndAcademicYearId(nameClasse, section.getId(), active.getId())) {
                    continue;
                }
                ClasseRoom classroom = new ClasseRoom();
                classroom.setNameClasse(nameClasse);
                classroom.setLevel(level);
                classroom.setCapacity(capacite);
                classroom.setSection(section);
                classroom.setAcademicYear(active);
                classroom.setDescription(nameClasse + " - " + level);
                classeRoomRepository.save(classroom);
                log.info("Classe créée : {} ({})", nameClasse, sectionLibelle);
            }
        });
    }

    private void seedMontants() {
        List<ClasseRoom> classes = classeRoomRepository.findAll();
        for (ClasseRoom classe : classes) {
            BigDecimal frais = switch (classe.getSection().getLibelle()) {
                case "Maternelle" -> BigDecimal.valueOf(15000);
                case "Primaire" -> BigDecimal.valueOf(25000);
                default -> BigDecimal.valueOf(35000);
            };
            montantRepository
                    .findByClasseRoomIdAndTypePaiement(classe.getId(), TypePaiement.FRAIS_PREINSCRIPTION)
                    .ifPresentOrElse(existing -> {
                    }, () -> {
                        Montant montant = new Montant();
                        montant.setCount(frais);
                        montant.setClasseRoom(classe);
                        montant.setTypePaiement(TypePaiement.FRAIS_PREINSCRIPTION);
                        montantRepository.save(montant);
                        log.info("Montant préinscription créé : {} -> {}", classe.getNameClasse(), frais);
                    });
        }
    }

    private void seedStudents() {
        if (studentRepository.count() > 0) {
            return;
        }
        List<StudentDTO> eleves = List.of(
                student("Awa", "Diop", LocalDate.of(2016, 3, 12), Gender.FEMININ,
                        "Mariama", "Diop", "mariama.diop@example.com", "+221771234501"),
                student("Mamadou", "Ndiaye", LocalDate.of(2015, 7, 25), Gender.MASCULIN,
                        "Ousmane", "Ndiaye", "ousmane.ndiaye@example.com", "+221771234502"),
                student("Fatou", "Sarr", LocalDate.of(2013, 1, 9), Gender.FEMININ,
                        "Aly", "Sarr", "aly.sarr@example.com", "+221771234503"),
                student("Ibrahima", "Fall", LocalDate.of(2011, 11, 3), Gender.MASCULIN,
                        "Khady", "Fall", "khady.fall@example.com", "+221771234504"),
                student("Aminata", "Gueye", LocalDate.of(2009, 5, 19), Gender.FEMININ,
                        "Cheikh", "Gueye", "cheikh.gueye@example.com", "+221771234505"),
                student("Babacar", "Ba", LocalDate.of(2007, 2, 28), Gender.MASCULIN,
                        "Ndèye", "Ba", "ndeye.ba@example.com", "+221771234506"));

        eleves.forEach(eleve -> {
            StudentDTO cree = studentCommandService.createStudentWithGuardian(eleve);
            log.info("Élève créé : {} {}", cree.getFirstNameStudent(), cree.getLastNameStudent());
        });
    }

    /**
     * Seed d'un pipeline complet de pré-inscription jusqu'à la validation :
     * un dossier DRAFT, un SUBMITTED, un UNDER_REVIEW et un APPROVED.
     * Le workflow est exécuté via les services métier réels afin de garantir
     * la cohérence des contraintes (tuteurs, documents obligatoires, frais).
     */
    private void seedPreEnrollments() {
        if (preEnrollmentRepository.count() > 0) {
            return;
        }
        AcademicYear active = academicYearRepository.findByStatutCode(true).orElse(null);
        if (active == null) {
            log.info("Aucune année scolaire active : seed des pré-inscriptions ignoré.");
            return;
        }
        seedPreEnrollmentDraft(active);
        seedPreEnrollmentSubmitted(active);
        seedPreEnrollmentUnderReview(active);
        seedPreEnrollmentApproved(active);
    }

    /** Dossier DRAFT : candidat + tuteur, sans documents ni soumission. */
    private void seedPreEnrollmentDraft(AcademicYear active) {
        Long id = createPreEnrollment(
                active, "Amadou", "Kane", LocalDate.of(2016, 8, 14), Gender.MASCULIN,
                "CP", "Primaire", "amadou.kane@example.com");
        addGuardian(id, RelationshipType.MOTHER, "Astou", "Kane", "astou.kane@example.com", "+221771234507");
        BasicInfo create = new BasicInfo(id, "CP", "Primaire");
        seedDocuments(create);
        log.info("Pré-inscription DRAFT créée : {}", id);
    }

    /** Dossier SUBMITTED : tuteur + documents obligatoires + frais vérifiés puis soumission. */
    private void seedPreEnrollmentSubmitted(AcademicYear active) {
        Long id = createPreEnrollment(
                active, "Salimata", "Sy", LocalDate.of(2015, 5, 3), Gender.FEMININ,
                "CE1", "Primaire", "salimata.sy@example.com");
        addGuardian(id, RelationshipType.FATHER, "Moussa", "Sy", "moussa.sy@example.com", "+221771234508");
        BasicInfo info = new BasicInfo(id, "CE1", "Primaire");
        seedDocuments(info);
        seedFee(info, "mock-trx-syb-001");
        Long submitted = preEnrollmentCommandService.submit(id).getId();
        log.info("Pré-inscription SUBMITTED créée : {}", submitted);
    }

    /** Dossier UNDER_REVIEW : soumis puis passage en revue. */
    private void seedPreEnrollmentUnderReview(AcademicYear active) {
        Long id = createPreEnrollment(
                active, "Oumar", "Cisse", LocalDate.of(2013, 12, 20), Gender.MASCULIN,
                "6eme", "Secondaire", "oumar.cisse@example.com");
        addGuardian(id, RelationshipType.MOTHER, "Bineta", "Cisse", "bineta.cisse@example.com", "+221771234509");
        BasicInfo info = new BasicInfo(id, "6eme", "Secondaire");
        seedDocuments(info);
        seedFee(info, "mock-trx-cis-002");
        preEnrollmentCommandService.submit(id);
        Long underReview = preEnrollmentCommandService.startReview(id, REVIEWER_ID).getId();
        log.info("Pré-inscription UNDER_REVIEW créée : {}", underReview);
    }

    /** Dossier APPROVED : frais vérifiés, documents approuvés puis validation finale. */
    private void seedPreEnrollmentApproved(AcademicYear active) {
        Long id = createPreEnrollment(
                active, "Aissatou", "Mbaye", LocalDate.of(2008, 4, 17), Gender.FEMININ,
                "2nde", "Secondaire", "aissatou.mbaye@example.com");
        addGuardian(id, RelationshipType.FATHER, "El Hadj", "Mbaye", "elhaj.mbaye@example.com", "+221771234510");
        BasicInfo info = new BasicInfo(id, "2nde", "Secondaire");
        List<Long> docIds = seedDocuments(info);
        seedFee(info, "mock-trx-mby-003");
        preEnrollmentCommandService.submit(id);
        preEnrollmentCommandService.startReview(id, REVIEWER_ID);
        docIds.forEach(documentId ->
                preEnrollmentCommandService.reviewDocument(
                        id, documentId, DocumentReviewStatus.APPROVED, REVIEWER_ID, "Document conforme"));
        Long approved = preEnrollmentCommandService.approve(id, REVIEWER_ID).getId();
        log.info("Pré-inscription APPROVED créée : {}", approved);
    }

    /** Crée le brouillon de pré-inscription (candidat + frais requis du cycle). */
    private Long createPreEnrollment(
            AcademicYear active, String firstName, String lastName, LocalDate birthDate, Gender gender,
            String nameClasse, String sectionLibelle, String email) {
        BigDecimal requiredFee = montantForClass(nameClasse, sectionLibelle);
        CreatePreEnrollmentRequest request = new CreatePreEnrollmentRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setBirthDate(birthDate);
        request.setGender(gender);
        request.setBirthPlace("Dakar");
        request.setAcademicYearId(active.getId());
        request.setRequestedLevel(nameClasse);
        request.setRequiredFee(requiredFee);
        PreEnrollmentResponse created = preEnrollmentCommandService.createDraft(request);
        return created.getId();
    }

    /** Ajoute un tuteur principal contactable. */
    private void addGuardian(Long preEnrollmentId, RelationshipType relationshipType, String firstName,
                             String lastName, String email, String phone) {
        AddPreEnrollmentGuardianRequest guardian = new AddPreEnrollmentGuardianRequest();
        guardian.setRelationshipType(relationshipType);
        guardian.setFirstName(firstName);
        guardian.setLastName(lastName);
        guardian.setEmail(email);
        guardian.setPhoneNumber(phone);
        guardian.setAddress("Dakar, Sénégal");
        guardian.setPrimaryContact(true);
        guardian.setFinancialResponsible(true);
        guardian.setEmergencyContact(true);
        preEnrollmentCommandService.addGuardian(preEnrollmentId, guardian);
    }

    /** Dépose les pièces obligatoires (acte de naissance + bulletin) et renvoie leurs identifiants. */
    private List<Long> seedDocuments(BasicInfo info) {
        AddPreEnrollmentDocumentRequest birth = new AddPreEnrollmentDocumentRequest();
        birth.setDocumentType("BIRTH_CERTIFICATE");
        birth.setStorageReference("/mock/pre-enrollments/" + info.id + "/birth-certificate.pdf");
        preEnrollmentCommandService.addDocument(info.id, birth);

        AddPreEnrollmentDocumentRequest report = new AddPreEnrollmentDocumentRequest();
        report.setDocumentType("REPORT_CARD");
        report.setStorageReference("/mock/pre-enrollments/" + info.id + "/report-card.pdf");
        preEnrollmentCommandService.addDocument(info.id, report);

        return preEnrollmentRepository.findById(info.id)
                .map(p -> p.getDocuments().stream().map(doc -> doc.getId()).toList())
                .orElse(List.of());
    }

    /** Enregistre et vérifie le versement des frais de préinscription du cycle. */
    private void seedFee(BasicInfo info, String transactionReference) {
        BigDecimal amount = montantForClass(info.nameClasse, info.sectionLibelle);
        RecordPreEnrollmentFeePaymentRequest request = new RecordPreEnrollmentFeePaymentRequest();
        request.setAmount(amount);
        request.setPaymentDate(LocalDate.now());
        request.setTransactionReference(transactionReference);
        request.setReceiptNumber("REC-" + transactionReference.toUpperCase());
        Long paymentId = preEnrollmentFeePaymentService.record(info.id, request).getId();
        preEnrollmentFeePaymentService.verify(paymentId);
    }

    /** Montant FRAIS_PREINSCRIPTION de la classe du cycle, 0 si non configuré. */
    private BigDecimal montantForClass(String nameClasse, String sectionLibelle) {
        AcademicYear active = academicYearRepository.findByStatutCode(true).orElse(null);
        if (active == null) {
            return BigDecimal.ZERO;
        }
        Optional<ClasseRoom> classe = classeRoomRepository.findAll().stream()
                .filter(c -> c.getAcademicYear() != null
                        && active.getId().equals(c.getAcademicYear().getId())
                        && nameClasse.equalsIgnoreCase(c.getNameClasse())
                        && c.getSection() != null && sectionLibelle.equalsIgnoreCase(c.getSection().getLibelle()))
                .findFirst();
        if (classe.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return montantRepository
                .findByClasseRoomIdAndTypePaiement(classe.get().getId(), TypePaiement.FRAIS_PREINSCRIPTION)
                .map(Montant::getCount)
                .orElse(BigDecimal.ZERO);
    }

    private static final long REVIEWER_ID = 1L;

    /** Porteur léger des informations d'un dossier en cours de seed. */
    private record BasicInfo(Long id, String nameClasse, String sectionLibelle) {
    }

    private StudentDTO student(String prenom, String nom, LocalDate naissance, Gender genre,
                               String prenomTuteur, String nomTuteur, String email, String telephone) {
        StudentDTO dto = new StudentDTO();
        dto.setFirstNameStudent(prenom);
        dto.setLastNameStudent(nom);
        dto.setDateOfBirth(naissance);
        dto.setGender(genre);
        dto.setEcolePrecedente("Ecole publique de Dakar");

        GuardianDTO tuteur = new GuardianDTO();
        tuteur.setFirstNameGuardian(prenomTuteur);
        tuteur.setLastNameGuardian(nomTuteur);
        tuteur.setEmail(email);
        tuteur.setPhoneNumber(telephone);
        tuteur.setAddress("Dakar, Sénégal");
        tuteur.setOccupation("Commerçant(e)");
        dto.setGuardian(tuteur);
        return dto;
    }
}