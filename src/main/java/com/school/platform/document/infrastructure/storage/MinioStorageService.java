package com.school.platform.document.infrastructure.storage;

import com.school.platform.shared.domain.exception.BadRequestException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stockage objet des documents sur MinIO (compatible S3).
 *
 * <p>Le chemin retourne est directement utilisable comme reference de stockage, de la forme
 * <code>dossier/{uuid}.{extension}</code>. Cette reference est persistee dans la
 * base de donnees (ex: {@code pre_enrollment_documents.storage_reference}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private static final Map<String, String> DEFAULT_CONTENT_TYPES = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp");

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /** Televerse un fichier et retourne sa reference de stockage. */
    public String store(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est requis.");
        }

        String extension = extensionOf(file.getOriginalFilename());
        String objectKey = normalizeFolder(folder) + "/" + UUID.randomUUID() + "." + extension;

        try (InputStream input = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(input, file.getSize(), -1)
                    .contentType(contentTypeOf(file, extension))
                    .build());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Echec du stockage du fichier sur MinIO", e);
        }
        return objectKey;
    }

    /** Recupere le contenu d'un objet MinIO a partir de sa reference de stockage. */
    public byte[] load(String objectKey) throws IOException {
        if (objectKey == null || objectKey.isBlank()) {
            throw new BadRequestException("La reference de stockage est requise.");
        }
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectKey.trim())
                .build())) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            stream.transferTo(buffer);
            return buffer.toByteArray();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Fichier introuvable dans MinIO: " + objectKey, e);
        }
    }

    /** Supprime un objet MinIO a partir de sa reference de stockage. */
    public void delete(String objectKey) throws IOException {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey.trim())
                    .build());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Echec de suppression du fichier dans MinIO", e);
        }
    }

    /** Nom logique du fichier (dernier segment de la reference). */
    public String fileName(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return "document";
        }
        String trimmed = objectKey.trim();
        return trimmed.substring(trimmed.lastIndexOf('/') + 1);
    }

    /** Verifie / cree le bucket au demarrage, sans bloquer l'application si MinIO est indisponible. */
    @PostConstruct
    void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
                log.info("Bucket MinIO '{}' cree.", properties.getBucket());
            }
        } catch (Exception e) {
            log.warn("MinIO indisponible au demarrage, bucket '{}' non verifie : {}",
                    properties.getBucket(), e.getMessage());
        }
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            throw new BadRequestException("Le dossier de stockage est requis.");
        }
        String normalized = folder.replace("\\", "/").trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.contains("..")) {
            throw new BadRequestException("Chemin de stockage invalide.");
        }
        return normalized;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Nom de fichier invalide.");
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            throw new BadRequestException("Extension de fichier manquante.");
        }
        String extension = originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
        Set<String> allowed = Set.copyOf(properties.getAllowedExtensions());
        if (!allowed.contains(extension)) {
            throw new BadRequestException("Extension de fichier non autorisee.");
        }
        return extension;
    }

    private String contentTypeOf(MultipartFile file, String extension) {
        String fromFile = file.getContentType();
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile;
        }
        return DEFAULT_CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");
    }
}