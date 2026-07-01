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
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_code", columnNames = "code"))
public class Role extends AuditableEntity {

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new LinkedHashSet<>();

    protected Role() {
    }

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void addPermission(Permission permission) {
        boolean exists = rolePermissions.stream().anyMatch(item -> item.getPermission().equals(permission));
        if (!exists) {
            rolePermissions.add(new RolePermission(this, permission));
        }
    }

    public Set<Permission> permissions() {
        return rolePermissions.stream().map(RolePermission::getPermission).collect(Collectors.toCollection(LinkedHashSet::new));
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
