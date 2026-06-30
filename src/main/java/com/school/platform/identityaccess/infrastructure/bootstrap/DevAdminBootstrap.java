package com.school.platform.identityaccess.infrastructure.bootstrap;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.domain.model.Users;
import com.school.platform.identityaccess.domain.model.UsersProfil;
import com.school.platform.identityaccess.infrastructure.persistence.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevAdminBootstrap implements ApplicationRunner {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || usersRepository.count() > 0) {
            return;
        }

        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "Bootstrap admin is enabled, but app.bootstrap.admin.email/password are not configured");
        }

        Users user = new Users();
        user.setUsername(adminEmail);
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setActif(true);

        UsersProfil profile = new UsersProfil();
        profile.setEmailUser(adminEmail);
        profile.setFirstName("Admin");
        profile.setLastName("System");
        profile.setRoleType(RoleType.ADMIN);
        profile.setUser(user);

        user.setProfils(List.of(profile));
        usersRepository.save(user);

        log.warn("Created development admin account: {}", adminEmail);
    }
}
