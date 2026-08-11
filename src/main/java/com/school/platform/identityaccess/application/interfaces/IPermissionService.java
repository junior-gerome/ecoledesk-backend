package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.application.dto.permission.PermissionFullDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPermissionService {
    PermissionFullDTO create(PermissionFullDTO dto);
    PermissionFullDTO update(Long id, PermissionFullDTO dto);
    PermissionFullDTO findById(Long id);
    Page<PermissionFullDTO> findAll(Pageable pageable);
    void delete(Long id);
}
