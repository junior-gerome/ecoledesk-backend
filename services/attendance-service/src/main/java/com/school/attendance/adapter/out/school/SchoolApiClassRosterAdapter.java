package com.school.attendance.adapter.out.school;

import com.school.attendance.application.port.out.ClassRosterPort;
import com.school.attendance.application.port.out.ClassRosterStudent;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class SchoolApiClassRosterAdapter implements ClassRosterPort {
    private final RestClient schoolApi;

    SchoolApiClassRosterAdapter(RestClient schoolApiRestClient) {
        this.schoolApi = schoolApiRestClient;
    }

    @Override
    public List<ClassRosterStudent> findStudentsByClass(Long classId, Long schoolYearId) {
        if (classId == null || classId <= 0) {
            return List.of();
        }

        Long resolvedSchoolYearId = schoolYearId != null ? schoolYearId : fetchActiveSchoolYearId();
        String className = fetchClassName(classId);

        String uri = UriComponentsBuilder.fromPath("/inscription/by-class/{classId}")
                .queryParamIfPresent("anneeScolaireId", java.util.Optional.ofNullable(resolvedSchoolYearId))
                .buildAndExpand(classId)
                .toUriString();

        List<InscriptionPayload> inscriptions = schoolApi.get()
                .uri(uri)
                .headers(SchoolApiRequestAuth::applyBearerToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (inscriptions == null || inscriptions.isEmpty()) {
            return List.of();
        }

        return inscriptions.stream()
                .map(inscription -> toRosterStudent(inscription, classId, className))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ClassRosterStudent::studentName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Long fetchActiveSchoolYearId() {
        SchoolYearPayload payload = schoolApi.get()
                .uri("/annees-scolaires/active")
                .headers(SchoolApiRequestAuth::applyBearerToken)
                .retrieve()
                .body(SchoolYearPayload.class);
        return payload == null ? null : payload.id();
    }

    private String fetchClassName(Long classId) {
        ClassPayload payload = schoolApi.get()
                .uri("/classes/{classId}", classId)
                .headers(SchoolApiRequestAuth::applyBearerToken)
                .retrieve()
                .body(ClassPayload.class);
        if (payload == null || payload.nameClasse() == null || payload.nameClasse().isBlank()) {
            return "Classe";
        }
        return payload.nameClasse().trim();
    }

    private ClassRosterStudent toRosterStudent(InscriptionPayload inscription, Long classId, String className) {
        if (inscription == null || inscription.student() == null || inscription.student().id() == null) {
            return null;
        }

        Long studentId = inscription.student().id();
        String studentName = (safe(inscription.student().lastNameStudent()) + " " + safe(inscription.student().firstNameStudent()))
                .trim();
        if (studentName.isBlank()) {
            studentName = "Eleve " + studentId;
        }

        return new ClassRosterStudent(studentId, studentName, classId, className);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record InscriptionPayload(Long id, StudentPayload student, Long classeRoomId, Long anneeScolaireId) {
    }

    private record StudentPayload(Long id, String firstNameStudent, String lastNameStudent) {
    }

    private record SchoolYearPayload(Long id) {
    }

    private record ClassPayload(Long id, String nameClasse) {
    }
}
