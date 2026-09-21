package com.school.platform.enrollment.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;

import com.school.platform.identityaccess.domain.model.Person;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Student extends BaseEntity {

    @EqualsAndHashCode.Include
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @NotBlank(message = "Le numero d'etudiant est obligatoire")
    @Column(name = "student_number", nullable = false, unique = true, length = 50)
    private String studentNumber;

    @NotNull(message = "La date d'admission est obligatoire")
    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "current_level", length = 50)
    private String currentLevel;

    @Column(name = "ecole_precedente", length = 100)
    private String ecolePrecedente;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 40)
    private Set<StudentGuardian> studentGuardians = new HashSet<>();

    public String getFirstNameStudent() {
        return person == null ? null : person.getFirstName();
    }

    public void setFirstNameStudent(String firstNameStudent) {
        ensurePerson().setFirstName(firstNameStudent);
    }

    public String getLastNameStudent() {
        return person == null ? null : person.getLastName();
    }

    public void setLastNameStudent(String lastNameStudent) {
        ensurePerson().setLastName(lastNameStudent);
    }

    public LocalDate getDateOfBirth() {
        return person == null ? null : person.getBirthDate();
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        ensurePerson().setBirthDate(dateOfBirth);
    }

    public Gender getGender() {
        return person == null ? null : person.getGender();
    }

    public void setGender(Gender gender) {
        ensurePerson().setGender(gender);
    }

    public String getPhotoUrl() {
        return person == null ? null : person.getPhotoUrl();
    }

    public void setPhotoUrl(String photoUrl) {
        ensurePerson().setPhotoUrl(photoUrl);
    }

    public LocalDate getRegistrationDate() {
        return admissionDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.admissionDate = registrationDate == null ? null : registrationDate.toLocalDate();
    }

    /** Returns the primary legal guardian, or the first linked guardian. */
    public Guardian getGuardian() {
        return studentGuardians.stream()
                .filter(StudentGuardian::isPrimaryContact)
                .findFirst()
                .or(() -> studentGuardians.stream().findFirst())
                .map(StudentGuardian::getGuardian)
                .orElse(null);
    }

    /**
     * Compatibility for the single-guardian student flow. More complete flows
     * can add several {@link StudentGuardian} links directly.
     */
    public void setGuardian(Guardian guardian) {
        if (guardian == null) {
            removeAllGuardianLinks();
            return;
        }

        StudentGuardian existingLink = studentGuardians.stream()
                .filter(link -> isLinkedTo(link, guardian))
                .findFirst()
                .orElse(null);

        // Updating a student commonly supplies its current guardian again. Reuse
        // that persistent link instead of replacing it: replacing it can make
        // Hibernate insert the same (student_id, guardian_id) pair before the
        // orphan removal is flushed, violating the database uniqueness rule.
        if (existingLink != null) {
            studentGuardians.stream()
                    .filter(link -> link != existingLink)
                    .toList()
                    .forEach(this::removeGuardianLink);
            existingLink.setPrimaryContact(true);
            existingLink.getGuardian().getStudentGuardians().add(existingLink);
            return;
        }

        removeAllGuardianLinks();

        StudentGuardian link = new StudentGuardian();
        link.setStudent(this);
        link.setGuardian(guardian);
        link.setRelationshipType(RelationshipType.GUARDIAN);
        link.setPrimaryContact(true);
        studentGuardians.add(link);
        guardian.getStudentGuardians().add(link);
    }

    private boolean isLinkedTo(StudentGuardian link, Guardian guardian) {
        if (link.getGuardian() == guardian) {
            return true;
        }
        return link.getGuardian() != null
                && link.getGuardian().getId() != null
                && link.getGuardian().getId().equals(guardian.getId());
    }

    private void removeAllGuardianLinks() {
        studentGuardians.stream().toList().forEach(this::removeGuardianLink);
    }

    private void removeGuardianLink(StudentGuardian link) {
        studentGuardians.remove(link);
        if (link.getGuardian() != null) {
            link.getGuardian().getStudentGuardians().remove(link);
        }
    }

    private Person ensurePerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }
}
