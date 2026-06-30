package com.school.platform.staff.application.mapper;

import com.school.platform.identityaccess.application.mapper.PersonMapper;

import com.school.platform.staff.application.dto.employee.*;
import com.school.platform.staff.application.dto.position.PositionBasicDTO;
import com.school.platform.staff.domain.model.Employee;
import com.school.platform.staff.domain.model.EmployeePosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final PersonMapper personMapper;
    private final PositionMapper positionMapper;

    public EmployeeBasicDTO toBasicDTO(Employee employee) {
        if (employee == null) return null;
        
        EmployeeBasicDTO dto = new EmployeeBasicDTO();
        dto.setId(employee.getId());
        dto.setPerson(personMapper.toBasicDTO(employee.getPerson()));
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        return dto;
    }

    public EmployeeMediumDTO toMediumDTO(Employee employee) {
        if (employee == null) return null;
        
        EmployeeMediumDTO dto = new EmployeeMediumDTO();
        dto.setId(employee.getId());
        dto.setPerson(personMapper.toMediumDTO(employee.getPerson()));
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setHireDate(employee.getHireDate());
        dto.setActivePositionsCount((int) employee.getPositions().stream()
            .filter(ep -> ep.getEndDate() == null || ep.getEndDate().isAfter(LocalDate.now()))
            .count());
        return dto;
    }

    public EmployeeFullDTO toFullDTO(Employee employee) {
        if (employee == null) return null;
        
        EmployeeFullDTO dto = new EmployeeFullDTO();
        dto.setId(employee.getId());
        dto.setPerson(personMapper.toFullDTO(employee.getPerson()));
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setHireDate(employee.getHireDate());
        dto.setPositions(employee.getPositions().stream()
            .map(this::toEmployeePositionDTO)
            .collect(Collectors.toList()));
        dto.setCreationDate(employee.getCreationDate());
        dto.setUpdateDate(employee.getUpdateDate());
        return dto;
    }

    private EmployeeFullDTO.EmployeePositionDTO toEmployeePositionDTO(EmployeePosition ep) {
        EmployeeFullDTO.EmployeePositionDTO dto = new EmployeeFullDTO.EmployeePositionDTO();
        dto.setId(ep.getId());
        dto.setPosition(positionMapper.toBasicDTO(ep.getPosition()));
        dto.setStartDate(ep.getStartDate());
        dto.setEndDate(ep.getEndDate());
        dto.setActive(ep.getEndDate() == null || ep.getEndDate().isAfter(LocalDate.now()));
        return dto;
    }
}
