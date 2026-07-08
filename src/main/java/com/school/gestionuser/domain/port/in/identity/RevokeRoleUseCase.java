package com.school.gestionuser.domain.port.in.identity;

public interface RevokeRoleUseCase {
    void revokeRole(String userId, String roleCode, String revokedBy);
}