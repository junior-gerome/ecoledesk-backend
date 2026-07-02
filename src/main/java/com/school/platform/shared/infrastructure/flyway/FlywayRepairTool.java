package com.school.platform.shared.infrastructure.flyway;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.Flyway;

public final class FlywayRepairTool {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");

    private FlywayRepairTool() {
    }

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = FlywayRepairTool.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        }

        String url = option(args, "--url", resolve(properties.getProperty("spring.datasource.url")));
        String username = option(args, "--username", resolve(properties.getProperty("spring.datasource.username")));
        String password = option(args, "--password", resolve(properties.getProperty("spring.datasource.password")));
        String locations = option(args, "--locations", resolve(properties.getProperty("spring.flyway.locations", "classpath:db/migration")));

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations(locations)
                .baselineOnMigrate(true)
                .load();

        flyway.repair();
        System.out.println("Flyway schema history repaired for " + url);
    }

    private static String option(String[] args, String name, String defaultValue) {
        String prefix = name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return defaultValue == null ? "" : defaultValue;
    }

    private static String resolve(String value) {
        if (value == null) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String fallback = matcher.group(2) == null ? "" : matcher.group(2);
            String replacement = System.getenv(key);
            if (replacement == null) {
                replacement = System.getProperty(key, fallback);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}