package com.school.platform.identityaccess.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PhoneNumber {

    private static final Pattern FORMAT = Pattern.compile("^\\+?[0-9]{8,15}$");

    @Column(name = "value")
    private String value;

    protected PhoneNumber() {
    }
    
    private PhoneNumber(String value) {
        this.value = normalize(value);
    }
    
    public static PhoneNumber of(String value) { return value == null || value.isBlank() ? null : new PhoneNumber(value); }

    public String value() {
        return value;
    }
    
    private static String normalize(String rawValue) {
        String normalized = Objects.requireNonNull(rawValue, "phone number must not be null").replaceAll("[\\s().-]",
                "");
        if (!FORMAT.matcher(normalized).matches())
            throw new IllegalArgumentException("phone number must contain 8 to 15 digits");
        return normalized;
    }
    
    @Override 
    public boolean equals(Object other) {
        return other instanceof PhoneNumber phoneNumber && Objects.equals(value, phoneNumber.value);
    }
    
    @Override 
    public int hashCode() {
        return Objects.hash(value);
        
     }
}
