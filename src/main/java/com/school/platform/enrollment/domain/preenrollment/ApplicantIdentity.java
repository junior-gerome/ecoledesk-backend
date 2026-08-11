package com.school.platform.enrollment.domain.preenrollment;
import java.time.LocalDate;
import com.school.platform.enrollment.domain.model.Gender;
import jakarta.persistence.Embeddable; import jakarta.persistence.EnumType; import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Embeddable @Getter @Setter @NoArgsConstructor
public class ApplicantIdentity {
 private String firstName; private String lastName; private LocalDate birthDate;
 @Enumerated(EnumType.STRING) private Gender gender; private String birthPlace;
 public boolean isComplete() { return firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank() && birthDate != null && gender != null; }
}
