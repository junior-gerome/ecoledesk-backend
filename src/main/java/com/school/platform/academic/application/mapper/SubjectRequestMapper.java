package com.school.platform.academic.application.mapper;

import com.school.platform.academic.application.dto.subject.SubjectRequest;
import com.school.platform.academic.domain.model.Subject;
import com.school.platform.shared.application.mapper.EntityMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses={})
public interface SubjectRequestMapper extends EntityMapper<SubjectRequest,Subject> {

    SubjectRequest toDto(Subject entity);

    @Mapping(target = "active", ignore = true)
    Subject toEntity(SubjectRequest dto);

    @Mapping(target ="id", ignore =true)
    @Mapping(target = "active", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SubjectRequest dto, @MappingTarget Subject entity);

    List<SubjectRequest> toDto(List<Subject> entityList);

    List<Subject> toEntity(List<SubjectRequest> dtoList);
}
