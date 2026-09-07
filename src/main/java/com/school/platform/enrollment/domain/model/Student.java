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
        studentGuardians.forEach(link -> link.getGuardian().getStudentGuardians().remove(link));
        studentGuardians.clear();
        if (guardian == null) {
            return;
        }

        StudentGuardian link = new StudentGuardian();
        link.setStudent(this);
        link.setGuardian(guardian);
        link.setRelationshipType(RelationshipType.GUARDIAN);
        link.setPrimaryContact(true);
        studentGuardians.add(link);
        guardian.getStudentGuardians().add(link);
    }

    private Person ensurePerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }
}
