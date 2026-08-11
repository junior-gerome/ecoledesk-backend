package com.school.platform.enrollment.application.impl;

import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianBasicDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianFullDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianMediumDTO;
import com.school.platform.enrollment.application.interfaces.IGuardianService;
import com.school.platform.enrollment.application.mapper.GuardianMapper;
import com.school.platform.enrollment.application.mapper.GuardianProjectionMapper;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.infrastructure.persistence.GuardianRepository;
import com.school.platform.shared.domain.exception.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianServiceImpl implements IGuardianService {

    private final GuardianRepository guardianRepository;
    private final GuardianMapper mapper;
    private final GuardianProjectionMapper projectionMapper;

    // ── Legacy ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = "guardians", allEntries = true)
    public GuardianDTO createGuardian(GuardianDTO dto) {
        return mapper.toDto(guardianRepository.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuardianDTO> getAllGuardians() {
        return getAllGuardians(PageRequest.of(0, 500)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GuardianDTO> getAllGuardians(Pageable pageable) {
        return guardianRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "guardians", key = "#id")
    public GuardianDTO updateGuardian(Long id, GuardianDTO dto) {
        Guardian guardian = findGuardian(id);
        mapper.updateEntityFromDto(dto, guardian);
        Guardian updated = guardianRepository.save(guardian);
        log.info("Responsable legal mis a jour : {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "guardians", key = "#id")
    public void deleteGuardian(Long id) {
        guardianRepository.delete(findGuardian(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "guardians", key = "#id")
    public GuardianDTO getGuardianById(Long id) {
        return mapper.toDto(findGuardian(id));
    }

    // ── Projections ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<GuardianBasicDTO> getAllBasic() {
        return projectionMapper.toBasicDTOList(guardianRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GuardianMediumDTO> getAllMedium(Pageable pageable) {
        return guardianRepository.findAll(pageable).map(projectionMapper::toMediumDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public GuardianFullDTO getFullById(Long id) {
        Guardian guardian = guardianRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Guardian", "id", id));
        return projectionMapper.toFullDTO(guardian);
    }

    private Guardian findGuardian(Long id) {
        return guardianRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Responsable legal non trouve avec l'ID " + id));
    }
}
