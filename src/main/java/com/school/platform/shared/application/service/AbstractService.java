package com.school.platform.shared.application.service;

import com.school.platform.shared.domain.legacy.BaseEntity;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.application.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractService<D, E extends BaseEntity<ID>, ID, R extends JpaRepository<E, ID>, M extends EntityMapper<D, E>> implements IGenericService<D, E, ID>{
  protected final R repository;
  protected final M mapper;
  protected final String entityName;

    @Override
    @Transactional
    public D create(D dto) {
        E entity = mapper.toEntity(dto);
        E savedEntity = repository.save(entity);
        log.info("Created {}: {}", entityName, savedEntity.getId());
        return mapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<D> findById(ID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    public D update(ID id, D dto) {
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(entityName + " not found with ID: " + id));
        mapper.updateEntityFromDto(dto, existingEntity);
        E updatedEntity = repository.save(existingEntity);
        log.info("Updated {}: {}", entityName, updatedEntity.getId());
        return mapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entityName + " not found with ID: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted {}: {}", entityName, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }
}
