package com.school.platform.identityaccess.infrastructure.bootstrap;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.domain.model.valueobject.Email;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DevAdminBootstrap implements ApplicationRunner {
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}") private boolean enabled;
    @Value("${app.bootstrap.admin.email:}") private String adminEmail;
    @Value("${app.bootstrap.admin.password:}") private String adminPassword;

    @Override
@Transactional
    public void run(ApplicationArguments arguments) {

        if (!enabled) {
            log.info("Bootstrap administrateur désactivé.");
            return;
        }

        if (!StringUtils.hasText(adminEmail)
                || !StringUtils.hasText(adminPassword)) {

            throw new IllegalStateException(
                    "Bootstrap administrateur activé, mais l'email "
                            + "ou le mot de passe n'est pas configuré.");
        }

        if (userAccountRepository
                .existsByUsernameIgnoreCase(adminEmail)) {

            log.info(
                    "Le compte administrateur existe déjà : {}",
                    adminEmail);
            return;
        }

        Role admin = roleRepository
                .findByCodeIgnoreCase("ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "Le rôle ADMIN doit être créé par Flyway "
                                + "avant le bootstrap administrateur."));

        Person person = new Person();
        person.setFirstName("Admin");
        person.setLastName("System");
        person.setEmail(adminEmail);

        UserAccount account = new UserAccount();
        account.setPerson(person);
        account.setUsername(adminEmail);
        account.setPassword(
                passwordEncoder.encode(adminPassword));
        account.setEnabled(true);
        account.setRoles(Set.of(admin));

        userAccountRepository.save(account);

        log.warn(
                "Compte administrateur de développement créé : {}",
                adminEmail);
    }

}
