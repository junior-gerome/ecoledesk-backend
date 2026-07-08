package com.school.platform.staff.application.mapper;

import com.school.platform.staff.application.dto.position.*;
import com.school.platform.staff.domain.model.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public PositionBasicDTO toBasicDTO(Position position) {
        if (position == null) return null;
        
        PositionBasicDTO dto = new PositionBasicDTO();
        dto.setId(position.getId());
        dto.setCode(position.getCode());
        dto.setLabel(position.getLabel());
        return dto;
    }

    public PositionFullDTO toFullDTO(Position position) {
        if (position == null) return null;
        
        PositionFullDTO dto = new PositionFullDTO();
        dto.setId(position.getId());
        dto.setCode(position.getCode());
        dto.setLabel(position.getLabel());
        dto.setDescription(position.getDescription());
        dto.setActive(position.getActive());
        dto.setCreationDate(position.getCreationDate());
        dto.setUpdateDate(position.getUpdateDate());
        return dto;
    }

    public Position toEntity(PositionFullDTO dto) {
        if (dto == null) return null;
        
        Position position = new Position();
        position.setId(dto.getId());
        position.setCode(dto.getCode());
        position.setLabel(dto.getLabel());
        position.setDescription(dto.getDescription());
        position.setActive(dto.getActive());
        return position;
    }
}
