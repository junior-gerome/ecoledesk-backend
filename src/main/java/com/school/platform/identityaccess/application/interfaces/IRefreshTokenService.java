package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenFullDTO;
import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenMediumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRefreshTokenService {
    RefreshTokenFullDTO findById(Long id);
    Page<RefreshTokenMediumDTO> findAll(Pageable pageable);
    void revoke(Long id);
}
