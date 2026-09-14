package com.school.platform.enrollment.domain.preenrollment;

import java.time.LocalDate;
import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.infrastructure.persistence.converter.GenderConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ApplicantIdentity {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    @Convert(converter = GenderConverter.class)
    private Gender gender;

    private String birthPlace;

    public boolean isComplete() {
        return firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank() && birthDate != null && gender != null;
    }
}

