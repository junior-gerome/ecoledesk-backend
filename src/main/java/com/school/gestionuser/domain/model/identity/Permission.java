package com.school.gestionuser.domain.model.identity;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Permission fine sur une ressource et une action.
 *
 * Convention : resource:action (ex: students:read, grades:write, finances:admin)
 *
 * Avantage : @PreAuthorize("hasAuthority('students:read')") dans les controllers.
 * Les permissions sont entièrement gérées en base → configurable sans redéploiement.
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "code", unique = true),
    @Index(name = "idx_permission_resource", columnList = "resource")
})
public class Permission extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code; // Ex: "students:read", "grades:write", "finances:admin"

    @Column(name = "name", nullable = false, length = 150)
    private String name; // Ex: "Lecture des étudiants"

    @Column(name = "description", length = 500)
    private String description;

    /**
     * Resource = module fonctionnel de l'ERP.
     * Ex: students, teachers, grades, finances, library, timetable
     */
    @Column(name = "resource", nullable = false, length = 50)
    private String resource;

    /**
     * Action = opération CRUD ou métier.
     * Ex: read, write, delete, admin, approve, validate
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "system_permission", nullable = false)
    private boolean systemPermission = false;

    @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    protected Permission() {}

    public Permission(String code, String resource, String action, String name) {
        this.code = code;
        this.resource = resource;
        this.action = action;
        this.name = name;
    }

    // Static factory method
    public static Permission of(String resource, String action) {
        String code = resource + ":" + action;
        String name = action + " " + resource;
        return new Permission(code, resource, action, name);
    }

    // Getters/Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public boolean isSystemPermission() { return systemPermission; }
    public Set<RolePermission> getRolePermissions() { return rolePermissions; }
}