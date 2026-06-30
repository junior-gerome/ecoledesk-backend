package com.school.platform.search.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.platform.search.domain.model.SearchFavorite;

public interface SearchFavoriteRepository extends JpaRepository<SearchFavorite, String> {
    List<SearchFavorite> findAllByOrderByCreatedAtDesc();
}
