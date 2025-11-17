package com.school.service;


import org.springframework.stereotype.Service;

import com.school.management.dto.ClasseRoomDTO;

import java.util.List;
import java.util.Optional;

@Service
public interface ClassService {
    List<ClasseRoomDTO> getAllClasses();
    Optional<ClasseRoomDTO> getClassById(Long id);
    ClasseRoomDTO createClass(ClasseRoomDTO classDTO);
    ClasseRoomDTO updateClass(Long id, ClasseRoomDTO classDTO);
    void deleteClass(Long id);
}
