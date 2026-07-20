package com.school.platform.enrollment.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;

import com.school.platform.identityaccess.domain.model.Person;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parents")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Parent extends BaseEntity {

    @EqualsAndHashCode.Include
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentParent> studentParents = new HashSet<>();

    public String getFirstNameParent() {
        return person == null ? null : person.getFirstName();
    }

    public void setFirstNameParent(String firstNameParent) {
        ensurePerson().setFirstName(firstNameParent);
    }

    public String getLastNameParent() {
        return person == null ? null : person.getLastName();
    }

    public void setLastNameParent(String lastNameParent) {
        ensurePerson().setLastName(lastNameParent);
    }

    public String getEmail() {
        return person == null ? null : person.getEmail();
    }

    public void setEmail(String email) {
        ensurePerson().setEmail(email);
    }

    public String getPhoneNumber() {
        return person == null ? null : person.getPhone();
    }

    public void setPhoneNumber(String phoneNumber) {
        ensurePerson().setPhone(phoneNumber);
    }

    public String getAddress() {
        return person == null ? null : person.getAddress();
    }

    public void setAddress(String address) {
        ensurePerson().setAddress(address);
    }

    public String getPhotoUrl() {
        return person == null ? null : person.getPhotoUrl();
    }

    public void setPhotoUrl(String photoUrl) {
        ensurePerson().setPhotoUrl(photoUrl);
    }

    public String getProfessionParent() {
        return occupation;
    }

    public void setProfessionParent(String professionParent) {
        this.occupation = professionParent;
    }

    private Person ensurePerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }
}
