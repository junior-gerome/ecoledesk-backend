package com.school.platform.staff.application;

import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;

import com.school.platform.staff.application.dto.employee.*;
import com.school.platform.staff.application.mapper.EmployeeMapper;
import com.school.platform.staff.domain.model.Employee;
import com.school.platform.staff.domain.model.EmployeePosition;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.staff.domain.model.Position;
import com.school.platform.staff.infrastructure.persistence.EmployeePositionRepository;
import com.school.platform.staff.infrastructure.persistence.EmployeeRepository;
import com.school.platform.staff.infrastructure.persistence.PositionRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PersonRepository personRepository;
    private final PositionRepository positionRepository;
    private final EmployeePositionRepository employeePositionRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeFullDTO create(Long personId, String employeeNumber, LocalDate hireDate) {
        if (employeeRepository.existsByEmployeeNumber(employeeNumber)) {
            throw new ValidationException("Numéro d'employé déjà utilisé");
        }
        
        Person person = personRepository.findById(personId)
            .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        
        Employee employee = new Employee();
        employee.setPerson(person);
        employee.setEmployeeNumber(employeeNumber);
        employee.setHireDate(hireDate);
        
        employee = employeeRepository.save(employee);
        return employeeMapper.toFullDTO(employee);
    }

    public void assignPosition(Long employeeId, Long positionId, LocalDate startDate) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new NotFoundException("Employé non trouvé"));
        
        Position position = positionRepository.findById(positionId)
            .orElseThrow(() -> new NotFoundException("Position non trouvée"));
        
        EmployeePosition ep = new EmployeePosition();
        ep.setEmployee(employee);
        ep.setPosition(position);
        ep.setStartDate(startDate);
        
        employeePositionRepository.save(ep);
    }

    public void endPosition(Long employeePositionId, LocalDate endDate) {
        EmployeePosition ep = employeePositionRepository.findById(employeePositionId)
            .orElseThrow(() -> new NotFoundException("Position non trouvée"));
        
        ep.setEndDate(endDate);
        employeePositionRepository.save(ep);
    }

    @Transactional(readOnly = true)
    public EmployeeFullDTO findById(Long id) {
        Employee employee = employeeRepository.findByIdWithPositions(id)
            .orElseThrow(() -> new NotFoundException("Employé non trouvé"));
        return employeeMapper.toFullDTO(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeMediumDTO> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable)
            .map(employeeMapper::toMediumDTO);
    }
}
