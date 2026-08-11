package com.school.platform.identityaccess.domain.model.valueobject;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    private static final Pattern FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Column(name = "value")
    private String value;

    protected Email() { }

    private Email(String value) {
        this.value = normalize(value);
    }

    public static Email of(String value) {
        return value == null || value.isBlank() ? null : new Email(value);
    }

    public String value() { 
        return value;
    }

    private static String normalize(String rawValue) {
        String normalized = Objects.requireNonNull(rawValue, "email must not be null").trim().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(normalized).matches()) throw new IllegalArgumentException("email must have a valid format");
        return normalized;
    }

    @Override
    public boolean equals(Object other) { 
        return other instanceof Email email && Objects.equals(value, email.value);
    }

    
    @Override 
    public int hashCode() {
        return Objects.hash(value);
    }
}
