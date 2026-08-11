package com.school.platform.staff.application.impl;

import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;
import com.school.platform.shared.domain.exception.shared.NotFoundException;
import com.school.platform.staff.application.dto.StaffAssignmentBasicDTO;
import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import com.school.platform.staff.application.dto.StaffMemberCreateRequest;
import com.school.platform.staff.application.dto.StaffMemberFullDTO;
import com.school.platform.staff.application.dto.StaffMemberMediumDTO;
import com.school.platform.staff.application.interfaces.StaffMemberService;
import com.school.platform.staff.application.mapper.StaffAssignmentMapper;
import com.school.platform.staff.application.mapper.StaffMemberMapper;
import com.school.platform.staff.domain.model.EmployeeNumber;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.staff.infrastructure.persistence.StaffAssignmentRepository;
import com.school.platform.staff.infrastructure.persistence.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffMemberServiceImpl implements StaffMemberService {

    private final StaffMemberRepository staffMemberRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final PersonRepository personRepository;
    private final StaffMemberMapper staffMemberMapper;
    private final StaffAssignmentMapper staffAssignmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StaffMemberBasicDTO> getAllBasic() {
        return staffMemberMapper.toBasicDTOList(
                staffMemberRepository.findAllWithPerson()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffMemberMediumDTO> getAllMedium() {
        return staffMemberMapper.toMediumDTOList(
                staffMemberRepository.findAllWithPerson()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StaffMemberFullDTO getById(Long id) {
        StaffMember member = staffMemberRepository.findByIdWithPerson(id)
                .orElseThrow(() -> new NotFoundException("StaffMember", "id", id));

        StaffMemberFullDTO dto = staffMemberMapper.toFullDTO(member);
        List<StaffAssignmentBasicDTO> assignments = staffAssignmentMapper.toBasicDTOList(
                staffAssignmentRepository.findByStaffMemberId(id)
        );
        dto.setAssignments(assignments);
        return dto;
    }

    @Override
    @Transactional
    public StaffMemberFullDTO create(StaffMemberCreateRequest request) {
        if (staffMemberRepository.existsByEmployeeNumberValue(request.getEmployeeNumber())) {
            throw new IllegalArgumentException("Numéro employé déjà utilisé : " + request.getEmployeeNumber());
        }

        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setPhone(request.getPhone());
        person.setGender(request.getGender());
        person.setBirthDate(request.getBirthDate());
        person.setAddress(request.getAddress());
        person.setCity(request.getCity());
        person.setCountry(request.getCountry());
        person.setPhotoUrl(request.getPhotoUrl());
        Person savedPerson = personRepository.save(person);

        EmployeeNumber empNumber = new EmployeeNumber();
        empNumber.setValue(request.getEmployeeNumber());

        StaffMember member = new StaffMember();
        member.setPerson(savedPerson);
        member.setEmployeeNumber(empNumber);
        member.setEmploymentDate(request.getEmploymentDate());
        member.setSpeciality(request.getSpeciality());
        member.setLevel(request.getLevel());
        member.setCniNumber(request.getCniNumber());
        member.setCniPhotoUrl(request.getCniPhotoUrl());

        StaffMember saved = staffMemberRepository.save(member);
        StaffMemberFullDTO dto = staffMemberMapper.toFullDTO(saved);
        dto.setAssignments(List.of());
        return dto;
    }

    @Override
    @Transactional
    public StaffMemberFullDTO update(Long id, StaffMemberCreateRequest request) {
        StaffMember member = staffMemberRepository.findByIdWithPerson(id)
                .orElseThrow(() -> new NotFoundException("StaffMember", "id", id));

        if (!member.getEmployeeNumber().getValue().equals(request.getEmployeeNumber())
                && staffMemberRepository.existsByEmployeeNumberValue(request.getEmployeeNumber())) {
            throw new IllegalArgumentException("Numéro employé déjà utilisé : " + request.getEmployeeNumber());
        }

        Person person = member.getPerson();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setPhone(request.getPhone());
        person.setGender(request.getGender());
        person.setBirthDate(request.getBirthDate());
        person.setAddress(request.getAddress());
        person.setCity(request.getCity());
        person.setCountry(request.getCountry());
        person.setPhotoUrl(request.getPhotoUrl());

        member.getEmployeeNumber().setValue(request.getEmployeeNumber());
        member.setEmploymentDate(request.getEmploymentDate());
        member.setSpeciality(request.getSpeciality());
        member.setLevel(request.getLevel());
        member.setCniNumber(request.getCniNumber());
        member.setCniPhotoUrl(request.getCniPhotoUrl());

        StaffMember saved = staffMemberRepository.save(member);
        StaffMemberFullDTO dto = staffMemberMapper.toFullDTO(saved);
        dto.setAssignments(staffAssignmentMapper.toBasicDTOList(
                staffAssignmentRepository.findByStaffMemberId(id)
        ));
        return dto;
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        StaffMember member = staffMemberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StaffMember", "id", id));
        member.setActive(false);
        staffMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return staffMemberRepository.count();
    }
}
