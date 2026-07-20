package com.school.platform.identityaccess.infrastructure.bootstrap;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
        if (!enabled) return;
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("Bootstrap admin is enabled, but app.bootstrap.admin.email/password are not configured");
        }
        if (userAccountRepository.existsByUsernameIgnoreCase(adminEmail)) return;
        Role admin = roleRepository.findByCodeIgnoreCase("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Le role ADMIN doit etre initialise avant le bootstrap"));
        Person person = new Person();
        person.setFirstName("Admin");
        person.setLastName("System");
        person.setEmail(Email.of(adminEmail));
        UserAccount account = new UserAccount();
        account.setPerson(person);
        account.setUsername(adminEmail);
        account.setPassword(passwordEncoder.encode(adminPassword));
        account.setEnabled(true);
        account.setRoles(Set.of(admin));
        userAccountRepository.save(account);
        log.warn("Created development admin account: {}", adminEmail);
    }
}
