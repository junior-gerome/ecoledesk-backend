package com.school.service;


import org.springframework.stereotype.Service;

import com.school.management.dto.ClassDTO;

import java.util.List;
import java.util.Optional;

@Service
public interface ClassService {
    List<ClassDTO> getAllClasses();
    Optional<ClassDTO> getClassById(Long id);
    ClassDTO createClass(ClassDTO classDTO);
    ClassDTO updateClass(Long id, ClassDTO classDTO);
    void deleteClass(Long id);
}
