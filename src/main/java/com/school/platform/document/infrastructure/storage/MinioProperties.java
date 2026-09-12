package com.school.platform.document.infrastructure.storage;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Configuration du stockage objet MinIO (compatible S3). */
@Component
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioProperties {

    /** URL du serveur MinIO, ex: http://localhost:9000 */
    private String endpoint = "http://localhost:9000";

    /** Cle d'acces (access key). */
    private String accessKey = "minioadmin";

    /** Cle secrete (secret key). */
    private String secretKey = "minioadmin";

    /** Nom du bucket utilise pour les documents. */
    private String bucket = "school-documents";

    /** Region S3, par defaut us-east-1 pour MinIO. */
    private String region = "us-east-1";

    /** En developpement (true), les URLs https sont ramenees en http. */
    private boolean insecure = true;

    /** Extensions de fichier autorisees au televersement. */
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "webp", "pdf"));
}