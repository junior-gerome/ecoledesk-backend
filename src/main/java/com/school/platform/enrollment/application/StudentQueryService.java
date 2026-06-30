package com.school.platform.enrollment.application;

import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.mapper.StudentMapper;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentQueryService {

    private final StudentRepository studentRepository;
    private final InscriptionStudentRepository inscriptionStudentRepository;
    private final StudentMapper studentMapper;

    @Transactional(readOnly = true)
    public Page<StudentDTO> findAll(Pageable pageable) {
        return studentRepository.findByActiveTrue(pageable).map(studentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public StudentDTO findById(Long id) {
        return studentRepository.findById(id).map(studentMapper::toDto)
                .orElseThrow(() -> new com.school.platform.shared.domain.exception.ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> findByParent(Long parentId, Pageable pageable) {
        return studentRepository.findByParentId(parentId, pageable).map(studentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> findByClasseRoom(Long classeRoomId, Pageable pageable) {
        return inscriptionStudentRepository.findByClasseRoomId(classeRoomId, pageable)
                .map(inscription -> studentMapper.toDto(inscription.getStudent()));
    }

    @Transactional(readOnly = true)
    public java.util.List<StudentDTO> findAllActive() {
        return studentRepository.findByActiveTrue().stream().map(studentMapper::toDto).toList();
    }
}
