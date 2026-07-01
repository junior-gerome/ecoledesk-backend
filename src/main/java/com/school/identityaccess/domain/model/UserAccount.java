package com.school.identityaccess.domain.model;

import com.school.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(name = "uk_user_accounts_username", columnNames = "username"))
public class UserAccount extends AuditableEntity {

    @Column(nullable = false, length = 180)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProfile> profiles = new LinkedHashSet<>();

    protected UserAccount() {
    }

    private UserAccount(String username, String passwordHash, Person person) {
        this.username = normalizeUsername(username);
        this.passwordHash = passwordHash;
        this.person = person;
        this.active = true;
    }

    public static UserAccount create(String username, String passwordHash, Person person) {
        return new UserAccount(username, passwordHash, person);
    }

    public void assignProfile(Profile profile) {
        if (!profile.isActive()) {
            throw new IllegalArgumentException("Cannot assign an inactive profile");
        }
        boolean exists = profiles.stream().anyMatch(item -> item.getProfile().equals(profile));
        if (!exists) {
            profiles.add(new UserProfile(this, profile));
        }
    }

    public void markLogin(Clock clock) {
        this.lastLoginAt = LocalDateTime.now(clock);
    }

    public void deactivate() {
        this.active = false;
    }

    public Set<String> authorityCodes() {
        Set<String> authorities = new LinkedHashSet<>();
        for (UserProfile userProfile : profiles) {
            Profile profile = userProfile.getProfile();
            if (!profile.isActive()) {
                continue;
            }
            for (Role role : profile.roles()) {
                if (!role.isActive()) {
                    continue;
                }
                authorities.add("ROLE_" + role.getCode());
                authorities.addAll(role.permissions().stream().map(Permission::getCode).collect(Collectors.toSet()));
            }
        }
        return authorities;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public Person getPerson() {
        return person;
    }

    public Set<UserProfile> getProfiles() {
        return Set.copyOf(profiles);
    }

    private static String normalizeUsername(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
