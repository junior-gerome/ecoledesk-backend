package com.school.platform.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.SchoolPlatformApplication;
import com.school.platform.identityaccess.application.impl.UserAccountManagementServiceImpl;
import com.school.platform.identityaccess.web.UserManagementController;

class ModularMonolithArchitectureTest {

    @Test
    void springBootApplicationScansOnlyPlatformMonolith() {
        SpringBootApplication annotation = SchoolPlatformApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(annotation.scanBasePackages()).containsExactly("com.school.platform");
    }

    @Test
    void legacyGestionUserModuleIsRemoved() {
        assertThat(Path.of("src/main/java/com/school/gestionuser")).doesNotExist();
    }

    @Test
    void platformCodeDoesNotDependOnLegacyGestionUserPackage() throws IOException {
        try (var files = Files.walk(Path.of("src/main/java/com/school/platform"))) {
            var forbiddenReferences = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> contains(path, "com.school.gestionuser"))
                    .toList();

            assertThat(forbiddenReferences).isEmpty();
        }
    }

    @Test
    void canonicalUserAccountApiIsActive() {
        assertThat(UserAccountManagementServiceImpl.class.isAnnotationPresent(Service.class)).isTrue();
        assertThat(UserManagementController.class.isAnnotationPresent(RestController.class)).isTrue();

        RequestMapping mapping = UserManagementController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping.value()).contains("/user-accounts");
    }

    private boolean contains(Path path, String text) {
        try {
            return Files.readString(path).contains(text);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + path, ex);
        }
    }
}