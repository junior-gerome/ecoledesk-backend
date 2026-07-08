package com.school.platform.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.school.platform.SchoolPlatformApplication;
import com.school.platform.identityaccess.application.UserAccountService;
import com.school.platform.identityaccess.web.UserAccountController;

class ModularMonolithArchitectureTest {

    @Test
    void springBootApplicationScansOnlyPlatformMonolith() {
        SpringBootApplication annotation = SchoolPlatformApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(annotation.scanBasePackages()).containsExactly("com.school.platform");
    }

    @Test
    void legacyGestionUserSourcesStayOutsideMainBuild() throws IOException {
        String buildFile = Files.readString(Path.of("build.gradle"));

        assertThat(buildFile).contains("exclude 'com/school/gestionuser/**'");
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
    void legacyUserAccountApiRequiresExplicitFeatureFlag() {
        assertLegacyFlag(UserAccountController.class.getAnnotation(ConditionalOnProperty.class));
        assertLegacyFlag(UserAccountService.class.getAnnotation(ConditionalOnProperty.class));
    }

    private void assertLegacyFlag(ConditionalOnProperty annotation) {
        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).containsExactly("school.identity.legacy-user-account.enabled");
        assertThat(annotation.havingValue()).isEqualTo("true");
    }

    private boolean contains(Path path, String text) {
        try {
            return Files.readString(path).contains(text);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + path, ex);
        }
    }
}
