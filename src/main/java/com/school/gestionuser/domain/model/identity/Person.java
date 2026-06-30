package com.school.gestionuser.domain.model.identity;

import com.school.gestionuser.domain.model.common.Address;
import com.school.gestionuser.domain.model.common.BaseEntity;
import com.school.gestionuser.domain.model.common.Gender;
import com.school.gestionuser.domain.model.profile.BusinessProfile;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * AGGREGATE ROOT pour l'identité physique d'une personne.
 *
 * Sépare intentionnellement :
 * - Les données personnelles (Person) : prénom, nom, date de naissance...
 * - Le compte d'accès (User) : login, mot de passe...
 * - Les profils métiers (BusinessProfile) : étudiant, enseignant...
 *
 * Une Person peut exister sans User (parent sans accès numérique).
 * Une Person peut avoir N BusinessProfiles simultanément.
 */
@Entity
@Table(name = "persons", indexes = {
    @Index(name = "idx_person_email", columnList = "email"),
    @Index(name = "idx_person_national_id", columnList = "national_id"),
    @Index(name = "idx_person_lastname", columnList = "last_name")
})
public class Person extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    /**
     * Numéro national d'identification (CNI, passeport, etc.)
     * Unique mais nullable (pas toujours disponible).
     */
    @Column(name = "national_id", unique = true, length = 50)
    private String nationalId;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Embedded
    private Address address;

    /**
     * Relation 1:1 optionnelle vers User.
     * mappedBy = la FK est côté User (person_id dans la table users).
     * CascadeType.ALL : si on supprime la Person, on supprime aussi le User.
     * Cette cascade doit être réfléchie selon les règles RGPD.
     */
    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, optional = true)
    private User user;

    /**
     * Collection de profils métiers de cette personne.
     * Une même Person peut être STUDENT et PARENT simultanément.
     * FetchType.LAZY obligatoire pour éviter N+1 queries.
     */
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BusinessProfile> businessProfiles = new HashSet<>();

    protected Person() {}

    public Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Domain methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasBusinessProfile(String profileTypeCode) {
        return businessProfiles.stream()
            .anyMatch(bp -> bp.getProfileType().getCode().equals(profileTypeCode)
                       && bp.isActive());
    }

    public void addBusinessProfile(BusinessProfile profile) {
        businessProfiles.add(profile);
        profile.setPerson(this);
    }

    public void removeBusinessProfile(BusinessProfile profile) {
        businessProfiles.remove(profile);
        profile.setPerson(null);
    }

    // Getters/Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Set<BusinessProfile> getBusinessProfiles() { return businessProfiles; }
}