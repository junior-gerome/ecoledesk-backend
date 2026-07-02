package com.school.platform.shared.application.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

public interface EntityMapper<D,E> {
D toDto(E entity);

E toEntity(D dto);

@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void updateEntityFromDto(D dto, @MappingTarget E entity);

List<D> toDto(List<E> entityList);

List<E> toEntity(List<D> dtoList);

}
