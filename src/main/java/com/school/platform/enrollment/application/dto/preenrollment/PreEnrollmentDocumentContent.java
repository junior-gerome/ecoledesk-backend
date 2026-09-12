package com.school.platform.enrollment.application.dto.preenrollment;

/** Contenu d'un document de preinscription destine au telechargement. */
public record PreEnrollmentDocumentContent(
        Long documentId,
        String fileName,
        String contentType,
        byte[] content) {
}