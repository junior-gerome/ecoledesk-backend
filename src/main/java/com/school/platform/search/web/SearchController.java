package com.school.platform.search.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.search.application.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> searchStudents(
            @RequestBody(required = false) Map<String, Object> criteria) {
        return ResponseEntity.ok(searchService.searchStudents(criteria(criteria)));
    }

    @PostMapping("/payments")
    public ResponseEntity<List<Map<String, Object>>> searchPayments(
            @RequestBody(required = false) Map<String, Object> criteria) {
        return ResponseEntity.ok(searchService.searchPayments(criteria(criteria)));
    }

    @PostMapping("/grades")
    public ResponseEntity<List<Map<String, Object>>> searchGrades(
            @RequestBody(required = false) Map<String, Object> criteria) {
        return ResponseEntity.ok(searchService.searchGrades(criteria(criteria)));
    }

    @PostMapping("/classes")
    public ResponseEntity<List<Map<String, Object>>> searchClasses(
            @RequestBody(required = false) Map<String, Object> criteria) {
        return ResponseEntity.ok(searchService.searchClasses(criteria(criteria)));
    }

    @PostMapping("/teachers")
    public ResponseEntity<List<Map<String, Object>>> searchTeachers(
            @RequestBody(required = false) Map<String, Object> criteria) {
        return ResponseEntity.ok(searchService.searchTeachers(criteria(criteria)));
    }

    @GetMapping("/global")
    public ResponseEntity<List<Map<String, Object>>> globalSearch(@RequestParam(name = "q") String query) {
        return ResponseEntity.ok(searchService.globalSearch(query));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> suggestions(@RequestParam(name = "q", required = false) String query) {
        return ResponseEntity.ok(searchService.suggestions(query));
    }

    @PostMapping("/favorites")
    public ResponseEntity<Map<String, Object>> saveFavoriteSearch(
            @RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(searchService.saveFavoriteSearch(payload == null ? Map.of() : payload));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<Map<String, Object>>> getFavoriteSearches() {
        return ResponseEntity.ok(searchService.getFavoriteSearches());
    }

    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<Void> deleteFavoriteSearch(@PathVariable String id) {
        searchService.deleteFavoriteSearch(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> criteria(Map<String, Object> criteria) {
        return criteria == null ? Map.of() : criteria;
    }
}
