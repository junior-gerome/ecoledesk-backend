package com.school.identityaccess.domain.model;

import com.school.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "persons", uniqueConstraints = @UniqueConstraint(name = "uk_persons_email", columnNames = "email"))
public class Person extends AuditableEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    protected Person() {
    }

    private Person(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.email = normalizeEmail(email);
        this.phoneNumber = blankToNull(phoneNumber);
    }

    public static Person create(String firstName, String lastName, String email, String phoneNumber) {
        return new Person(firstName, lastName, email, phoneNumber);
    }

    public void rename(String firstName, String lastName) {
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
    }

    public void updateContact(String email, String phoneNumber) {
        this.email = normalizeEmail(email);
        this.phoneNumber = blankToNull(phoneNumber);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private static String normalizeEmail(String value) {
        return requireText(value, "email").toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String field) {
        String text = Objects.requireNonNull(value, field + " is required").trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return text;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
