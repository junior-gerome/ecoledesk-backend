package com.school.platform.attendance.web;

import com.school.platform.attendance.domain.model.Absence;
import com.school.platform.enrollment.domain.model.InscriptionStudent;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository.ClassRosterRow;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final InscriptionStudentRepository inscriptionRepo;
    private final AbsenceRepository absenceRepo;
    private final StudentRepository studentRepo;

    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<Map<String, Object>>> getRecords(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, name = "status") String statuses,
            @RequestParam(required = false) Boolean justified,
            @RequestParam(required = false) Long anneeScolaireId) {

        if (classId != null && date != null) {
            List<ClassRosterRow> roster = findRosterRows(classId, anneeScolaireId);
            List<Long> studentIds = roster.stream()
                    .map(ClassRosterRow::getStudentId)
                    .collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            Map<Long, Absence> recordByStudent = absenceRepo
                    .findByStudentIdInAndDate(studentIds, date)
                    .stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a, (a, b) -> a));

            List<Map<String, Object>> records = roster.stream()
                    .map(row -> toAttendanceRecord(recordByStudent.get(row.getStudentId()), row, date))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(records);
        }

        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();

        List<Absence> records;
        if (classId != null) {
            List<Long> studentIds = findInscriptions(classId, anneeScolaireId).stream()
                    .map(i -> i.getStudent().getId())
                    .collect(Collectors.toList());
            records = studentIds.isEmpty()
                    ? List.of()
                    : absenceRepo.findByStudentIdInAndDateBetween(studentIds, start, end);
        } else {
            records = absenceRepo.findByDateBetween(start, end);
        }

        List<String> statusFilter = statuses == null || statuses.isBlank()
                ? List.of()
                : Arrays.stream(statuses.split(","))
                        .map(String::trim)
                        .filter(status -> !status.isBlank())
                        .collect(Collectors.toList());

        List<Map<String, Object>> response = records.stream()
                .filter(record -> statusFilter.isEmpty() || statusFilter.contains(record.getStatus()))
                .filter(record -> justified == null || Boolean.valueOf(Boolean.TRUE.equals(record.getJustified())).equals(justified))
                .map(record -> toAttendanceRecord(record, record.getStudent(), findInscription(record.getStudent().getId()), record.getDate()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    @Transactional
    public ResponseEntity<List<Map<String, Object>>> saveDaily(@RequestBody Map<String, Object> payload) {
        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le corps de la requete est requis.");
        }

        Long classId = asLong(payload.get("classId"));
        LocalDate date = asDate(payload.get("date"));

        if (classId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'identifiant de la classe est requis.");
        }
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de pointage est requise au format ISO yyyy-MM-dd.");
        }

        Object rawEntries = payload.get("entries");
        if (!(rawEntries instanceof List<?> entries)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La liste des pointages est requise.");
        }

        List<Map<String, Object>> saved = new ArrayList<>();
        for (Object rawEntry : entries) {
            if (!(rawEntry instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> entry = rawMap.entrySet().stream()
                    .filter(item -> item.getKey() != null)
                    .collect(Collectors.toMap(
                            item -> String.valueOf(item.getKey()),
                            Map.Entry::getValue,
                            (left, right) -> right,
                            HashMap::new));
            Long studentId = asLong(entry.get("studentId"));
            if (studentId == null) {
                continue;
            }
            Student student = studentRepo.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }

            Absence record = absenceRepo.findByStudentIdAndDate(studentId, date).orElseGet(Absence::new);
            record.setStudent(student);
            record.setDate(date);
            record.setStatus(validStatus(entry.getOrDefault("status", "PRESENT")));
            record.setHours(validHours(entry.get("hours")));
            if (record.getJustified() == null) {
                record.setJustified(false);
            }

            Absence persisted = absenceRepo.save(record);
            saved.add(toAttendanceRecord(persisted, student, findInscription(studentId), date));
        }

        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/records/{id}/justification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateJustification(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Absence record = absenceRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pointage introuvable"));
        record.setJustified(Boolean.TRUE.equals(payload.get("justified")));
        Object note = payload.get("justificationNote");
        record.setJustificationNote(note == null ? null : String.valueOf(note));
        Absence saved = absenceRepo.save(record);
        return ResponseEntity.ok(toAttendanceRecord(saved, saved.getStudent(), findInscription(saved.getStudent().getId()), saved.getDate()));
    }

    @DeleteMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Transactional
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        absenceRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<Map<String, Object>>> getSummary(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        List<Absence> records;

        if (classId != null) {
            List<Long> studentIds = inscriptionRepo.findByClasseRoomId(classId).stream()
                    .map(i -> i.getStudent().getId())
                    .collect(Collectors.toList());
            records = studentIds.isEmpty()
                    ? List.of()
                    : absenceRepo.findByStudentIdInAndDateBetween(studentIds, start, end);
        } else {
            records = absenceRepo.findByDateBetween(start, end);
        }

        Map<Long, List<Absence>> byStudent = records.stream()
                .filter(record -> !"PRESENT".equals(record.getStatus()))
                .collect(Collectors.groupingBy(record -> record.getStudent().getId()));

        List<Map<String, Object>> rows = byStudent.entrySet().stream().map(entry -> {
            Student student = entry.getValue().get(0).getStudent();
            InscriptionStudent inscription = findInscription(student.getId());
            long absences = entry.getValue().stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
            long lates = entry.getValue().stream().filter(a -> "LATE".equals(a.getStatus())).count();
            long unjustified = entry.getValue().stream().filter(a -> !Boolean.TRUE.equals(a.getJustified())).count();
            LocalDate lastDate = entry.getValue().stream().map(Absence::getDate).max(LocalDate::compareTo).orElse(start);

            Map<String, Object> row = new HashMap<>();
            row.put("studentId", student.getId());
            row.put("studentName", fullName(student));
            row.put("className", inscription == null || inscription.getClasseRoom() == null ? "Non affecte" : inscription.getClasseRoom().getNameClasse());
            row.put("totalAbsences", absences);
            row.put("totalLates", lates);
            row.put("unjustifiedCount", unjustified);
            row.put("lastDate", lastDate.toString());
            return row;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    private Map<String, Object> toAttendanceRecord(Absence record, Student student, InscriptionStudent inscription, LocalDate date) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", record == null ? 0 : record.getId());
        row.put("studentId", student.getId());
        row.put("studentName", fullName(student));
        row.put("firstName", student.getFirstNameStudent());
        row.put("lastName", student.getLastNameStudent());
        row.put("classId", inscription == null || inscription.getClasseRoom() == null ? 0 : inscription.getClasseRoom().getId());
        row.put("className", inscription == null || inscription.getClasseRoom() == null ? "Non affecte" : inscription.getClasseRoom().getNameClasse());
        row.put("date", date.toString());
        row.put("status", record == null ? "PRESENT" : record.getStatus());
        row.put("hours", record == null || record.getHours() == null ? 0 : record.getHours());
        row.put("justified", record != null && Boolean.TRUE.equals(record.getJustified()));
        row.put("justificationNote", record == null ? null : record.getJustificationNote());
        row.put("updatedAt", record == null || record.getUpdatedAt() == null ? LocalDateTime.now().toString() : record.getUpdatedAt().toString());
        return row;
    }

    private Map<String, Object> toAttendanceRecord(Absence record, ClassRosterRow roster, LocalDate date) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", record == null ? 0 : record.getId());
        row.put("studentId", roster.getStudentId());
        row.put("studentName", fullName(roster.getFirstNameStudent(), roster.getLastNameStudent(), roster.getStudentId()));
        row.put("firstName", roster.getFirstNameStudent());
        row.put("lastName", roster.getLastNameStudent());
        row.put("classId", roster.getClassId());
        row.put("className", roster.getClassName() == null || roster.getClassName().isBlank() ? "Non affecte" : roster.getClassName());
        row.put("date", date.toString());
        row.put("status", record == null ? "PRESENT" : record.getStatus());
        row.put("hours", record == null || record.getHours() == null ? 0 : record.getHours());
        row.put("justified", record != null && Boolean.TRUE.equals(record.getJustified()));
        row.put("justificationNote", record == null ? null : record.getJustificationNote());
        row.put("updatedAt", record == null || record.getUpdatedAt() == null ? LocalDateTime.now().toString() : record.getUpdatedAt().toString());
        return row;
    }

    private InscriptionStudent findInscription(Long studentId) {
        return inscriptionRepo.findFirstByStudentIdOrderByIdDesc(studentId).orElse(null);
    }

    private List<InscriptionStudent> findInscriptions(Long classId, Long anneeScolaireId) {
        if (anneeScolaireId == null) {
            return inscriptionRepo.findByClasseRoomId(classId);
        }
        List<InscriptionStudent> inscriptions =
                inscriptionRepo.findByClasseRoomIdAndAnneeScolaireId(classId, anneeScolaireId);
        return inscriptions.isEmpty() ? inscriptionRepo.findByClasseRoomId(classId) : inscriptions;
    }

    private List<ClassRosterRow> findRosterRows(Long classId, Long anneeScolaireId) {
        List<ClassRosterRow> roster = inscriptionRepo.findClassRosterRowsByClass(classId, anneeScolaireId);
        return roster.isEmpty() && anneeScolaireId != null
                ? inscriptionRepo.findClassRosterRowsByClass(classId, null)
                : roster;
    }

    private String fullName(Student student) {
        return student.getFirstNameStudent() + " " + student.getLastNameStudent();
    }

    private String fullName(String firstName, String lastName, Long studentId) {
        String name = ((firstName == null ? "" : firstName.trim()) + " " + (lastName == null ? "" : lastName.trim())).trim();
        return name.isBlank() ? "Eleve " + studentId : name;
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        String text = String.valueOf(value).trim();
        if (text.isBlank()) return null;
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiant numerique invalide: " + text);
        }
    }

    private Integer asInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        String text = String.valueOf(value).trim();
        if (text.isBlank()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre d'heures invalide: " + text);
        }
    }

    private Integer validHours(Object value) {
        Integer hours = asInt(value);
        if (hours < 0 || hours > 24) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nombre d'heures doit etre compris entre 0 et 24.");
        }
        return hours;
    }

    private String validStatus(Object value) {
        String status = value == null ? "PRESENT" : String.valueOf(value).trim().toUpperCase();
        if (!List.of("PRESENT", "ABSENT", "LATE", "EXCUSED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut de presence invalide: " + value);
        }
        return status;
    }

    private LocalDate asDate(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date invalide: " + value);
        }
    }
}
