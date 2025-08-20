package com.school.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.enums.TypeDocument;
import com.school.management.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStudentId(Long studentId);
    List<Document> findByTypeDocument(TypeDocument typeDocument);
    List<Document> findByStudentIdAndTypeDocument(Long studentId, TypeDocument typeDocument);
}
