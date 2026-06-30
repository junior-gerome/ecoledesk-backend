package com.school.platform.staff.application;

import com.school.platform.staff.application.dto.position.*;
import com.school.platform.staff.application.mapper.PositionMapper;
import com.school.platform.staff.domain.model.Position;
import com.school.platform.staff.infrastructure.persistence.PositionRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    public PositionFullDTO create(PositionFullDTO dto) {
        if (positionRepository.existsByCode(dto.getCode())) {
            throw new ValidationException("Code position déjà utilisé");
        }
        Position position = positionMapper.toEntity(dto);
        position = positionRepository.save(position);
        return positionMapper.toFullDTO(position);
    }

    public PositionFullDTO update(Long id, PositionFullDTO dto) {
        Position position = positionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Position non trouvée"));
        
        position.setLabel(dto.getLabel());
        position.setDescription(dto.getDescription());
        
        position = positionRepository.save(position);
        return positionMapper.toFullDTO(position);
    }

    @Transactional(readOnly = true)
    public PositionFullDTO findById(Long id) {
        Position position = positionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Position non trouvée"));
        return positionMapper.toFullDTO(position);
    }

    @Transactional(readOnly = true)
    public Page<PositionFullDTO> findAll(Pageable pageable) {
        return positionRepository.findAll(pageable)
            .map(positionMapper::toFullDTO);
    }

    public void delete(Long id) {
        Position position = positionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Position non trouvée"));
        position.setActive(false);
        positionRepository.save(position);
    }
}
