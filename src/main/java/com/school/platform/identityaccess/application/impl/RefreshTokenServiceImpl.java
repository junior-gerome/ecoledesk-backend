package com.school.platform.identityaccess.application.impl;

import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenFullDTO;
import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenMediumDTO;
import com.school.platform.identityaccess.application.interfaces.IRefreshTokenService;
import com.school.platform.identityaccess.application.mapper.RefreshTokenMapper;
import com.school.platform.identityaccess.domain.model.RefreshToken;
import com.school.platform.identityaccess.infrastructure.persistence.RefreshTokenRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenFullDTO findById(Long id) {
        RefreshToken token = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Refresh token non trouve"));
        return refreshTokenMapper.toFullDTO(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefreshTokenMediumDTO> findAll(Pageable pageable) {
        return refreshTokenRepository.findAll(pageable).map(refreshTokenMapper::toMediumDTO);
    }

    @Override
    public void revoke(Long id) {
        RefreshToken token = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Refresh token non trouve"));
        token.revoke(null);
        refreshTokenRepository.save(token);
    }
}
