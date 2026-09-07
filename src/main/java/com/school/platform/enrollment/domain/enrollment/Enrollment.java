package com.school.platform.enrollment.domain.enrollment;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(name = "uk_enrollments_pre_enrollment", columnNames = "pre_enrollment_id"))
@Getter
@NoArgsConstructor
public class Enrollment extends BaseEntity {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "number", nullable = false, unique = true, length = 50))
    private EnrollmentNumber number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_enrollment_id", nullable = false)
    private PreEnrollment preEnrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private ClasseRoom classroom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnrollmentType type = EnrollmentType.NEW_ADMISSION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnrollmentStatus status = EnrollmentStatus.PENDING_CONFIRMATION;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate = LocalDate.now();

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "withdrawal_date")
    private LocalDate withdrawalDate;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public static Enrollment pending(EnrollmentNumber number, PreEnrollment preEnrollment, AcademicYear academicYear, ClasseRoom classroom) {
        Enrollment enrollment = new Enrollment();
        enrollment.number = number;
        enrollment.preEnrollment = preEnrollment;
        enrollment.student = null;
        enrollment.academicYear = academicYear;
        enrollment.classroom = classroom;
        return enrollment;
    }

    public static Enrollment pending(EnrollmentNumber number, PreEnrollment preEnrollment, Student student, AcademicYear academicYear, ClasseRoom classroom) {
        Enrollment enrollment = new Enrollment();
        enrollment.number = number;
        enrollment.preEnrollment = preEnrollment;
        enrollment.student = student;
        enrollment.academicYear = academicYear;
        enrollment.classroom = classroom;
        return enrollment;
    }

    public void confirm(Student student) {
        if (status != EnrollmentStatus.PENDING_CONFIRMATION)
            throw new IllegalStateException("Only pending enrollment can be confirmed");
        if (student == null)
            throw new IllegalStateException("Student is required for confirmation");
        if (classroom == null)
            throw new IllegalStateException("Classroom is required for confirmation");
        this.student = student;
        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmationDate = LocalDate.now();
    }

    public void confirm() {
        confirm(this.student);
    }

    public void withdraw() {
        if (status != EnrollmentStatus.CONFIRMED)
            throw new IllegalStateException("Only a confirmed enrollment can be withdrawn");
        status = EnrollmentStatus.WITHDRAWN;
        withdrawalDate = LocalDate.now();
    }

    public void cancel(String reason) {
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Cancellation reason is required");
        status = EnrollmentStatus.CANCELLED;
        cancellationReason = reason.trim();
    }
}
