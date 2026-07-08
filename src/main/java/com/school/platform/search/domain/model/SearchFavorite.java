package com.school.platform.search.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "search_favorites")
public class SearchFavorite {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Lob
    @Column(
        name = "criteria_json",
        nullable = false,
        columnDefinition = "TEXT"
    )
    private String criteriaJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}