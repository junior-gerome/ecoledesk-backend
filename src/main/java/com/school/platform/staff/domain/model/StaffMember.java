package com.school.platform.staff.domain.model;

import java.time.LocalDate;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.enrollment.domain.model.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff_members")
@Getter
@Setter
@NoArgsConstructor
public class StaffMember extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    @NotNull
    private Person person;

    @Embedded
    private EmployeeNumber employeeNumber;

    @Column(name = "employment_date", nullable = false)
    private LocalDate employmentDate;

    @Column(name = "speciality", length = 100)
    private String speciality;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "cni_number", length = 50)
    private String cniNumber;

    @Column(name = "cni_photo_url", length = 500)
    private String cniPhotoUrl;

    // Transitional accessors keep academic clients stable while Teacher is removed.
    public String getFirstnameTeacher() { return person == null ? null : person.getFirstName(); }
    public void setFirstnameTeacher(String value) { ensurePerson().setFirstName(value); }
    public String getLastnameTeacher() { return person == null ? null : person.getLastName(); }
    public void setLastnameTeacher(String value) { ensurePerson().setLastName(value); }
    public String getEmail() { return person == null ? null : person.getEmail(); }
    public void setEmail(String value) { ensurePerson().setEmail(value); }
    public String getPhoneNumber() { return person == null ? null : person.getPhone(); }
    public void setPhoneNumber(String value) { ensurePerson().setPhone(value); }
    public Gender getGender() { return person == null ? null : person.getGender(); }
    public void setGender(Gender value) { ensurePerson().setGender(value); }
    public String getAdress() { return person == null ? null : person.getAddress(); }
    public void setAdress(String value) { ensurePerson().setAddress(value); }
    public String getPhotoUrl() { return person == null ? null : person.getPhotoUrl(); }
    public void setPhotoUrl(String value) { ensurePerson().setPhotoUrl(value); }
    public String getNiveau() { return level; }
    public void setNiveau(String value) { level = value; }
    public LocalDate getDateEmbauche() { return employmentDate; }
    public void setDateEmbauche(LocalDate value) { employmentDate = value; }

    private Person ensurePerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }
}
