package com.school.identityaccess.domain.model;

import com.school.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "profiles", uniqueConstraints = @UniqueConstraint(name = "uk_profiles_code", columnNames = "code"))
public class Profile extends AuditableEntity {

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProfileRole> profileRoles = new LinkedHashSet<>();

    protected Profile() {
    }

    public Profile(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void addRole(Role role) {
        boolean exists = profileRoles.stream().anyMatch(item -> item.getRole().equals(role));
        if (!exists) {
            profileRoles.add(new ProfileRole(this, role));
        }
    }

    public Set<Role> roles() {
        return profileRoles.stream().map(ProfileRole::getRole).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
