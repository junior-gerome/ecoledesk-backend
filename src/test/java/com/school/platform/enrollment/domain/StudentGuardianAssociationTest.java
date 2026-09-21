package com.school.platform.enrollment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.model.StudentGuardian;
import org.junit.jupiter.api.Test;

class StudentGuardianAssociationTest {

    @Test
    void settingTheCurrentGuardianAgainReusesTheExistingLink() {
        Student student = new Student();
        Guardian guardian = new Guardian();

        student.setGuardian(guardian);
        StudentGuardian originalLink = student.getStudentGuardians().iterator().next();

        student.setGuardian(guardian);

        assertEquals(1, student.getStudentGuardians().size());
        assertEquals(1, guardian.getStudentGuardians().size());
        assertSame(originalLink, student.getStudentGuardians().iterator().next());
        assertTrue(originalLink.isPrimaryContact());
    }
}
