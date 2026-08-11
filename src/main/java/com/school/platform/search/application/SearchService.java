package com.school.platform.search.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.billing.domain.model.Paiement;
import com.school.platform.search.domain.model.SearchFavorite;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;
import com.school.platform.search.infrastructure.persistence.SearchFavoriteRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.staff.infrastructure.persistence.StaffMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int BATCH_SIZE = 500;

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClasseRoomRepository classeRoomRepository;
    private final StaffMemberRepository teacherRepository;
    private final GradeRepository gradeRepository;
    private final PaiementRepository paiementRepository;
    private final SearchFavoriteRepository searchFavoriteRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchStudents(Map<String, Object> criteria) {
        String keyword = normalized(criteria, "keywords");
        String section = normalized(criteria, "section");
        String level = normalized(criteria, "level");
        String gender = normalized(criteria, "gender");
        Long classId = longValue(criteria.get("classId"));
        int limit = limit(criteria);
        Map<Long, Enrollment> enrollments = latestEnrollmentByStudent();

        return studentRepository.findAll(PageRequest.of(0, limit)).stream()
                .map(student -> studentRow(student, enrollments.get(student.getId())))
                .filter(row -> keyword.isBlank() || rowMatches(row, keyword))
                .filter(row -> section.isBlank() || sectionMatches(text(row.get("section")), section))
                .filter(row -> level.isBlank() || text(row.get("level")).toLowerCase(Locale.ROOT).contains(level))
                .filter(row -> gender.isBlank() || text(row.get("gender")).equalsIgnoreCase(gender))
                .filter(row -> classId == null || Objects.equals(row.get("classId"), classId))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchTeachers(Map<String, Object> criteria) {
        String keyword = normalized(criteria, "keywords");
        int limit = limit(criteria);

        return teacherRepository.findAll(PageRequest.of(0, limit)).stream()
                .map(this::teacherRow)
                .filter(row -> keyword.isBlank() || rowMatches(row, keyword))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchClasses(Map<String, Object> criteria) {
        String keyword = normalized(criteria, "keywords");
        String section = normalized(criteria, "section");
        String level = normalized(criteria, "level");
        int limit = limit(criteria);

        return classeRoomRepository.findAllWithDetails().stream()
                .map(this::classRow)
                .filter(row -> keyword.isBlank() || rowMatches(row, keyword))
                .filter(row -> section.isBlank() || sectionMatches(text(row.get("section")), section))
                .filter(row -> level.isBlank() || text(row.get("level")).toLowerCase(Locale.ROOT).contains(level))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchGrades(Map<String, Object> criteria) {
        String keyword = normalized(criteria, "keywords");
        int limit = limit(criteria);

        return gradeRepository.findAll(PageRequest.of(0, limit)).stream()
                .filter(grade -> gradeDateMatches(grade, criteria))
                .map(this::gradeRow)
                .filter(row -> keyword.isBlank() || rowMatches(row, keyword))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchPayments(Map<String, Object> criteria) {
        String keyword = normalized(criteria, "keywords");
        String status = normalized(criteria, "paymentStatus");
        int limit = limit(criteria);

        return paiementRepository.findAll(PageRequest.of(0, limit)).stream()
                .filter(payment -> paymentDateMatches(payment, criteria))
                .map(this::paymentRow)
                .filter(row -> keyword.isBlank() || rowMatches(row, keyword))
                .filter(row -> status.isBlank() || text(row.get("status")).equalsIgnoreCase(status))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> globalSearch(String query) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        criteria.put("keywords", query);
        criteria.put("size", 20);

        return Stream.of(
                        searchStudents(criteria).stream(),
                        searchTeachers(criteria).stream(),
                        searchClasses(criteria).stream())
                .flatMap(stream -> stream)
                .limit(30)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> suggestions(String query) {
        String keyword = normalize(query);
        if (keyword.isBlank()) {
            return List.of();
        }

        Map<Long, Enrollment> enrollments = latestEnrollmentByStudent();
        Stream<String> studentSuggestions = studentRepository.findAll(PageRequest.of(0, DEFAULT_LIMIT)).stream()
                .map(student -> studentRow(student, enrollments.get(student.getId())))
                .flatMap(row -> Stream.of(
                        text(row.get("firstName")) + " " + text(row.get("lastName")),
                        text(row.get("class")),
                        text(row.get("section"))));

        Stream<String> teacherSuggestions = teacherRepository.findAll(PageRequest.of(0, DEFAULT_LIMIT)).stream()
                .flatMap(teacher -> Stream.of(
                        safe(teacher.getFirstnameTeacher()) + " " + safe(teacher.getLastnameTeacher()),
                        safe(teacher.getSpeciality())));

        return Stream.concat(studentSuggestions, teacherSuggestions)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .filter(value -> normalize(value).contains(keyword))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .limit(10)
                .toList();
    }

    @Transactional
    public Map<String, Object> saveFavoriteSearch(Map<String, Object> payload) {
        SearchFavorite favorite = new SearchFavorite();
        favorite.setId(UUID.randomUUID().toString());
        favorite.setName(favoriteName(payload.get("name")));
        favorite.setCriteriaJson(toJson(criteriaMap(payload.get("criteria"))));

        SearchFavorite saved = searchFavoriteRepository.save(favorite);
        return favoriteRow(saved);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFavoriteSearches() {
        return searchFavoriteRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::favoriteRow)
                .toList();
    }

    @Transactional
    public void deleteFavoriteSearch(String id) {
        searchFavoriteRepository.deleteById(id);
    }

    private Map<Long, Enrollment> latestEnrollmentByStudent() {
        return enrollmentRepository.findByStatus(EnrollmentStatus.CONFIRMED).stream()
                .filter(e -> e.getStudent() != null && e.getStudent().getId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getStudent().getId(),
                        e -> e,
                        (left, right) -> left.getId() != null && right.getId() != null && left.getId() > right.getId() ? left : right,
                        LinkedHashMap::new));
    }

    private Map<String, Object> studentRow(Student student, Enrollment enrollment) {
        Map<String, Object> row = baseRow("student");
        row.put("id", student.getId());
        row.put("firstName", safe(student.getFirstNameStudent()));
        row.put("lastName", safe(student.getLastNameStudent()));
        row.put("gender", student.getGender() == null ? "" : student.getGender().name());
        row.put("date", student.getRegistrationDate() == null ? null : student.getRegistrationDate().toString());
        row.put("status", Boolean.FALSE.equals(student.getActive()) ? "INACTIVE" : "ACTIVE");

        if (enrollment != null && enrollment.getClassroom() != null) {
            ClasseRoom classeRoom = enrollment.getClassroom();
            row.put("classId", classeRoom.getId());
            row.put("class", safe(classeRoom.getNameClasse()));
            row.put("level", safe(classeRoom.getLevel()));
            row.put("section", classeRoom.getSection() == null ? "" : safe(classeRoom.getSection().getLibelle()));
        } else {
            row.put("classId", null);
            row.put("class", "Non inscrit");
            row.put("level", "");
            row.put("section", "");
        }

        return row;
    }

    private Map<String, Object> teacherRow(StaffMember teacher) {
        Map<String, Object> row = baseRow("teacher");
        row.put("id", teacher.getId());
        row.put("firstName", safe(teacher.getFirstnameTeacher()));
        row.put("lastName", safe(teacher.getLastnameTeacher()));
        row.put("subject", safe(teacher.getSpeciality()));
        row.put("status", "ACTIVE");
        return row;
    }

    private Map<String, Object> classRow(ClasseRoom classeRoom) {
        Map<String, Object> row = baseRow("class");
        row.put("id", classeRoom.getId());
        row.put("class", safe(classeRoom.getNameClasse()));
        row.put("level", safe(classeRoom.getLevel()));
        row.put("section", classeRoom.getSection() == null ? "" : safe(classeRoom.getSection().getLibelle()));
        row.put("status", Boolean.FALSE.equals(classeRoom.getActif()) ? "INACTIVE" : "ACTIVE");
        return row;
    }

    private Map<String, Object> gradeRow(Grade grade) {
        Map<String, Object> row = baseRow("grade");
        row.put("id", grade.getId());
        row.put("firstName", grade.getStudent() == null ? "" : safe(grade.getStudent().getFirstNameStudent()));
        row.put("lastName", grade.getStudent() == null ? "" : safe(grade.getStudent().getLastNameStudent()));
        row.put("class", grade.getClasse() == null ? "" : safe(grade.getClasse().getNameClasse()));
        row.put("subject", grade.getSubject() == null ? "" : safe(grade.getSubject().getNameSubject()));
        row.put("value", grade.getGrade() == null ? 0 : grade.getGrade().doubleValue());
        row.put("date", grade.getAssessmentDate() == null ? null : grade.getAssessmentDate().toString());
        row.put("status", safe(grade.getPeriod()));
        return row;
    }

    private Map<String, Object> paymentRow(Paiement payment) {
        Map<String, Object> row = baseRow("payment");
        row.put("id", payment.getId());
        row.put("firstName", payment.getStudent() == null ? "" : safe(payment.getStudent().getFirstNameStudent()));
        row.put("lastName", payment.getStudent() == null ? "" : safe(payment.getStudent().getLastNameStudent()));
        row.put("class", payment.getEnrollment() == null || payment.getEnrollment().getClassroom() == null
                ? ""
                : safe(payment.getEnrollment().getClassroom().getNameClasse()));
        row.put("amount", payment.getMontantPaye() == null ? BigDecimal.ZERO : payment.getMontantPaye());
        row.put("date", payment.getDatePaiement() == null ? null : payment.getDatePaiement().toString());
        row.put("status", payment.getMontantRestant() == null
                || payment.getMontantRestant().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PENDING");
        row.put("paymentType", payment.getTypePaiement() == null ? "" : payment.getTypePaiement().name());
        return row;
    }

    private Map<String, Object> baseRow(String type) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("type", type);
        return row;
    }

    private Map<String, Object> favoriteRow(SearchFavorite favorite) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", favorite.getId());
        row.put("name", favorite.getName());
        row.put("criteria", fromJson(favorite.getCriteriaJson()));
        row.put("createdAt", favorite.getCreatedAt());
        return row;
    }

    private String favoriteName(Object value) {
        String name = text(value).trim();
        return name.isBlank() ? "Recherche" : name;
    }

    private Map<String, Object> criteriaMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }

        Map<String, Object> criteria = new LinkedHashMap<>();
        rawMap.forEach((key, mapValue) -> {
            if (key != null) {
                criteria.put(String.valueOf(key), mapValue);
            }
        });
        return criteria;
    }

    private String toJson(Map<String, Object> criteria) {
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Critere de recherche favori invalide.", ex);
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private boolean rowMatches(Map<String, Object> row, String keyword) {
        return row.values().stream()
                .map(this::text)
                .map(this::normalize)
                .anyMatch(value -> value.contains(keyword));
    }

    private boolean sectionMatches(String actualSection, String requestedSection) {
        String normalizedSection = normalize(actualSection);
        if ("anglophone".equals(requestedSection)) {
            return normalizedSection.contains("anglo");
        }
        if ("francophone".equals(requestedSection)) {
            return normalizedSection.contains("franco") || !normalizedSection.contains("anglo");
        }
        return normalizedSection.contains(requestedSection);
    }

    private boolean gradeDateMatches(Grade grade, Map<String, Object> criteria) {
        if (grade.getAssessmentDate() == null) {
            return true;
        }
        return dateMatches(grade.getAssessmentDate(), criteria);
    }

    private boolean paymentDateMatches(Paiement payment, Map<String, Object> criteria) {
        if (payment.getDatePaiement() == null) {
            return true;
        }
        return dateMatches(payment.getDatePaiement().atStartOfDay(), criteria);
    }

    private boolean dateMatches(LocalDateTime value, Map<String, Object> criteria) {
        LocalDate start = dateValue(criteria.get("startDate"));
        LocalDate end = dateValue(criteria.get("endDate"));
        LocalDate current = value.toLocalDate();
        return (start == null || !current.isBefore(start))
                && (end == null || !current.isAfter(end));
    }

    private LocalDate dateValue(Object value) {
        if (value == null || text(value).isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text(value));
        } catch (RuntimeException ignored) {
                return null;
        }
    }

    private int limit(Map<String, Object> criteria) {
        Long value = longValue(criteria.get("size"));
        if (value == null || value <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.toIntExact(Math.min(value, 200));
    }

    private Long longValue(Object value) {
        if (value == null || text(value).isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalized(Map<String, Object> criteria, String key) {
        return normalize(text(criteria.get(key)));
    }

    private String normalize(String value) {
        return safe(value).toLowerCase(Locale.ROOT).trim();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
