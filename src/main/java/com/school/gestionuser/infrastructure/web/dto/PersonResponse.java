package com.school.gestionuser.infrastructure.web.dto;

import com.school.gestionuser.domain.model.common.Gender;
import com.school.gestionuser.domain.model.identity.Person;
import java.time.LocalDate;

public class PersonResponse {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String email;
    private String phoneNumber;

    public static PersonResponse from(Person person) {
        PersonResponse response = new PersonResponse();
        if (person != null) {
            response.id = person.getId();
            response.firstName = person.getFirstName();
            response.lastName = person.getLastName();
            response.birthDate = person.getDateOfBirth();
            response.gender = person.getGender();
            response.email = person.getEmail();
            response.phoneNumber = person.getPhone();
        }
        return response;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public Gender getGender() { return gender; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}