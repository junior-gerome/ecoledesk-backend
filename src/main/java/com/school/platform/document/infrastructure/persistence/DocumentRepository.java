package com.school.platform.document.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.document.domain.model.TypeDocument;
import com.school.platform.document.domain.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStudentId(Long studentId);
    List<Document> findByTypeDocument(TypeDocument typeDocument);
    List<Document> findByStudentIdAndTypeDocument(Long studentId, TypeDocument typeDocument);
}
