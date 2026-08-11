package com.school.platform.enrollment.domain.preenrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreEnrollmentNumber {

    @Column(name = "value", nullable = false, length = 50)
    private String value;

    private PreEnrollmentNumber(String value) {
        this.value = value;
    }

    public static PreEnrollmentNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Pre-enrollment number is required");
        }
        return new PreEnrollmentNumber(value.trim().toUpperCase(Locale.ROOT));
    }
}