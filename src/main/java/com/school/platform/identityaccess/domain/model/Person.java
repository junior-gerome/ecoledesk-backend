package com.school.platform.identityaccess.domain.model;

import java.time.LocalDate;

import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.identityaccess.domain.model.valueobject.Address;
import com.school.platform.identityaccess.domain.model.valueobject.BirthDate;
import com.school.platform.identityaccess.domain.model.valueobject.Email;
import com.school.platform.identityaccess.domain.model.valueobject.PhoneNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Person extends BaseEntity {

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le prenom est obligatoire")
    @Size(min = 2, max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Transient
    private String fullName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, length = 150))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", length = 20))
    private PhoneNumber phone;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "birth_date"))
    private BirthDate birthDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line", column = @Column(name = "address", length = 255)),
            @AttributeOverride(name = "city", column = @Column(name = "city", length = 100)),
            @AttributeOverride(name = "region", column = @Column(name = "region", length = 100)),
            @AttributeOverride(name = "country", column = @Column(name = "country", length = 100)),
            @AttributeOverride(name = "complement", column = @Column(name = "address_complement", length = 255))
    })
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    // Compatibility accessors keep the established DTO and repository contracts scalar.
    public String getEmail() {
        return email == null ? null : email.value();
    }
         
    // public void setEmail(Email email) {
    //     this.email = email; 
    // }

    public void setEmail(String value) { 
        this.email = Email.of(value); 
    }

    // public Email email() { 
    //     return email;
    // }

    public String getPhone() { 
        return phone == null ? null : phone.value();
     }
     
     public void setPhone(String value) {
         this.phone = PhoneNumber.of(value);
     }
         
     public PhoneNumber phoneNumber() {
         return phone;
     }
        

    public LocalDate getBirthDate() {
         return birthDate == null ? null : birthDate.value();
         }

    public void setBirthDate(LocalDate value) { 
        this.birthDate = BirthDate.of(value);
     }

    public BirthDate birthDate() {
         return birthDate;
     }

    public String getAddress() {
         return address == null ? null : address.line();
     }

    public void setAddress(String value) { 
        this.address = Address.of(value, getCity(), null, getCountry(), null);
     }

    public String getCity() { 
        return address == null ? null : address.city();
     }

    public void setCity(String value) { 
        this.address = Address.of(getAddress(), value, null, getCountry(), null);
     }

    public String getCountry() {
         return address == null ? null : address.country(); 
     }

     public void setCountry(String value) {
         this.address = Address.of(getAddress(), getCity(), null, value, null);
     }
     
    public Address address() { return address; }
}
