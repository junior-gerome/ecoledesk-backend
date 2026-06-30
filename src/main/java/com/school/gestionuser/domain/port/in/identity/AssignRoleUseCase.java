package com.school.gestionuser.domain.port.in.identity;

public interface AssignRoleUseCase {
    void assignRole(String userId, String roleCode, String assignedBy);
}