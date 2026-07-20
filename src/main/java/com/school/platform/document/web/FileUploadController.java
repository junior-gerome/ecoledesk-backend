package com.school.platform.document.web;

import com.school.platform.document.application.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        try {
            String fileUrl = fileStorageService.storeFile(file, type);
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{*fileUrl}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileUrl) {
        try {
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            byte[] fileData = fileStorageService.loadFile(relativePath);
            String fileName = Paths.get(relativePath).getFileName().toString();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(fileData);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            fileStorageService.deleteFile(fileUrl);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
