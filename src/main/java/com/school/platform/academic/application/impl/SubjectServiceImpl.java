package com.school.platform.academic.application.impl;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.SubjectDTO;
import com.school.platform.academic.application.interfaces.SubjectService;
import com.school.platform.academic.application.mapper.SubjectMapper;
import com.school.platform.academic.domain.model.Subject;
import com.school.platform.academic.infrastructure.persistence.SubjectRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
  private final SubjectRepository subjectRepository;
  private final SubjectMapper mapper;

  @Transactional
  public SubjectDTO createdSubject(SubjectDTO dto) {
    Subject entity = mapper.toEntity(dto);
    Subject saved = subjectRepository.save(entity);
    return mapper.toDto(saved);
  }

  @Transactional
  public SubjectDTO updatedSubject(Long id, SubjectDTO dto) {
    Subject subject = subjectRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Matier non trouver par ID:" + id));
    
            subject.setNameSubject(dto.getNameSubject());
            subject.setCoefficient(dto.getCoefficient());
            subject.setCode(dto.getCode());
            subject.setDescription(dto.getDescription());

    Subject updated = subjectRepository.save(subject);
    
    return mapper.toDto(updated);
  }

  @Transactional
  public List<SubjectDTO> getAllSubject(){
    return subjectRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
  }

  @Transactional
  public long getTotalSubjects(){
    return subjectRepository.count();
  }

  @Transactional
  public void deleteSubject(Long id){
    if(!subjectRepository.existsById(id)){
      throw new ResourceNotFoundException("matiere non trouvee avec l'id:" + id);
    }
    subjectRepository.deleteById(id);
  }

  @Transactional
  public SubjectDTO getSubjectById(Long id){
    return subjectRepository.findById(id).map(mapper::toDto).orElseThrow(()-> new ResourceNotFoundException("Matiere", "id", id));
  }

}
