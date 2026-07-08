package com.school.platform.shared.infrastructure.backup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class BackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private final String backupDir = "backups/";
    private final String dbName = "bd_gsbp";

    @Scheduled(cron = "0 0 1 * * *") // Tous les jours à 1h du matin
    public void performDailyBackup() {
        createBackup("daily");
    }

    @Scheduled(cron = "0 0 1 * * 1") // Tous les lundis à 1h du matin
    public void performWeeklyBackup() {
        createBackup("weekly");
    }

    @Scheduled(cron = "0 0 1 1 * *") // Le 1er du mois à 1h du matin
    public void performMonthlyBackup() {
        createBackup("monthly");
    }

    private void createBackup(String type) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String backupPath = backupDir + type + "/" + dbName + "_" + timestamp + ".sql";

            // Créer les répertoires nécessaires
            new File(backupDir + type).mkdirs();

            // Commande de backup MySQL
            ProcessBuilder processBuilder = new ProcessBuilder(
                "mysqldump",
                "-u" + dbUsername,
                "--databases",
                dbName
            );
            processBuilder.environment().put("MYSQL_PWD", dbPassword);
            processBuilder.redirectOutput(new File(backupPath));
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            
            Process process = processBuilder.start();
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("La sauvegarde a expiré après 5 minutes.");
            }

            // Vérifier si le backup a réussi
            if (process.exitValue() == 0) {
                // Compression du fichier
                ProcessBuilder gzipBuilder = new ProcessBuilder("gzip", backupPath);
                gzipBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
                process = gzipBuilder.start();
                finished = process.waitFor(5, TimeUnit.MINUTES);
                if (!finished) {
                    process.destroyForcibly();
                    throw new RuntimeException("La compression gzip a expiré.");
                }

                // Nettoyer les anciens backups
                cleanOldBackups(type);
            } else {
                throw new RuntimeException("mysqldump a échoué avec le code de sortie: " + process.exitValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde: " + e.getMessage(), e);
        }
    }

    private void cleanOldBackups(String type) {
        File backupFolder = new File(backupDir + type);
        File[] files = backupFolder.listFiles();
        if (files == null) return;

        // Garder uniquement les N derniers backups
        int keepCount;
        switch (type) {
            case "daily":
                keepCount = 7;  // Garder 7 jours
                break;
            case "weekly":
                keepCount = 4;  // Garder 4 semaines
                break;
            case "monthly":
                keepCount = 12; // Garder 12 mois
                break;
            default:
                keepCount = 7;
        }

        // Trier les fichiers par date de modification
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        // Supprimer les fichiers excédentaires
        for (int i = keepCount; i < files.length; i++) {
            files[i].delete();
        }
    }

    public void restoreFromBackup(String backupFile) {
        try {
            // Décompresser si nécessaire
            if (backupFile.endsWith(".gz")) {
                ProcessBuilder gunzipBuilder = new ProcessBuilder("gunzip", backupFile);
                gunzipBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
                Process process = gunzipBuilder.start();
                boolean finished = process.waitFor(5, TimeUnit.MINUTES);
                if (!finished) {
                    process.destroyForcibly();
                    throw new RuntimeException("La décompression gunzip a expiré.");
                }
                backupFile = backupFile.substring(0, backupFile.length() - 3);
            }

            // Restaurer la base de données
            ProcessBuilder restoreBuilder = new ProcessBuilder(
                "mysql",
                "-u" + dbUsername,
                dbName
            );
            restoreBuilder.environment().put("MYSQL_PWD", dbPassword);
            restoreBuilder.redirectInput(new File(backupFile));
            restoreBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            
            Process process = restoreBuilder.start();
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("La restauration a expiré.");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("Erreur lors de la restauration: mysql a échoué avec le code " + process.exitValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la restauration: " + e.getMessage(), e);
        }
    }
}
