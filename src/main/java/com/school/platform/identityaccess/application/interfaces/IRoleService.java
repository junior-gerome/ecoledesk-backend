package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.application.dto.role.RoleFullDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface IRoleService {
    RoleFullDTO create(RoleFullDTO dto);
    RoleFullDTO update(Long id, RoleFullDTO dto);
    void assignPermissions(Long roleId, Set<Long> permissionIds);
    RoleFullDTO findById(Long id);
    Page<RoleFullDTO> findAll(Pageable pageable);
    void delete(Long id);
}
