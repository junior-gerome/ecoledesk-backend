package com.school.platform.document.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.school.platform.shared.domain.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_DIRECTORIES = Set.of(
            "photos/students",
            "photos/parents",
            "photos/teachers",
            "documents",
            "documents/students",
            "documents/parents",
            "documents/teachers");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "pdf");

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est requis.");
        }

        String normalizedDirectory = normalizeDirectory(subDirectory);
        String extension = extensionOf(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        Path uploadPath = safeResolve(normalizedDirectory);

        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName).normalize();
        ensureInsideUploadRoot(filePath);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return normalizedDirectory + "/" + fileName;
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl != null && !fileUrl.isBlank()) {
            Path filePath = safeResolve(fileUrl);
            Files.deleteIfExists(filePath);
        }
    }

    public byte[] loadFile(String fileUrl) throws IOException {
        Path filePath = safeResolve(fileUrl);
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("Fichier introuvable");
        }
        return Files.readAllBytes(filePath);
    }

    private String normalizeDirectory(String subDirectory) {
        if (subDirectory == null || subDirectory.isBlank()) {
            throw new BadRequestException("Le type de fichier est requis.");
        }

        String normalized = subDirectory.replace("\\", "/").trim();
        if (!ALLOWED_DIRECTORIES.contains(normalized)) {
            throw new BadRequestException("Type de fichier non autorise.");
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
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Extension de fichier non autorisee.");
        }
        return extension;
    }

    private Path safeResolve(String relativePath) {
        Path root = uploadRoot();
        Path target = root.resolve(relativePath.replace("\\", "/")).normalize();
        ensureInsideUploadRoot(target);
        return target;
    }

    private void ensureInsideUploadRoot(Path target) {
        if (!target.startsWith(uploadRoot())) {
            throw new BadRequestException("Chemin de fichier invalide.");
        }
    }

    private Path uploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
