package com.school.platform.enrollment.application;

import com.school.platform.enrollment.application.dto.ParentDTO;
import com.school.platform.enrollment.application.mapper.ParentMapper;
import com.school.platform.enrollment.domain.model.Parent;
import com.school.platform.enrollment.infrastructure.persistence.ParentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper mapper;

    @Transactional
    @CacheEvict(value = "parents", allEntries = true)
    public ParentDTO createParent(ParentDTO dto) {
        Parent entity = mapper.toEntity(dto);
        Parent saved = parentRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional
    public List<ParentDTO> getAllParents() {
        return getAllParents(PageRequest.of(0, 500)).getContent();
    }

    @Transactional
    public Page<ParentDTO> getAllParents(Pageable pageable) {
        return parentRepository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional
    @CacheEvict(value = "parents", key = "#id")
    public ParentDTO updateParent(Long id, ParentDTO dto) {
        Parent existingParent = parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouve avec l'ID " + id));

        mapper.updateEntityFromDto(dto, existingParent);

        Parent updated = parentRepository.save(existingParent);

        log.info("Parent mis a jour avec succes : {}", updated.getId());
        return mapper.toDto(updated);
    }

    @Transactional
    @CacheEvict(value = "parents", key = "#id")
    public void deleteParent(Long id) {
        Parent existingParent = parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouve avec l'ID " + id));
        parentRepository.delete(existingParent);
    }

    @Transactional
    @Cacheable(value = "parents", key = "#id")
    public ParentDTO getParentById(Long id) {
        return parentRepository.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouve avec l'ID " + id));
    }
}
