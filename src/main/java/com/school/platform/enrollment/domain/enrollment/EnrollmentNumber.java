package com.school.platform.enrollment.domain.enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentNumber {

    @Column(name = "value", nullable = false, length = 50)
    private String value;

    private EnrollmentNumber(String value) {
        this.value = value;
    }

    public static EnrollmentNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Enrollment number is required");
        }
        return new EnrollmentNumber(value.trim().toUpperCase(Locale.ROOT));
    }
}