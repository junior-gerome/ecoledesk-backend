package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


import com.school.exception.ResourceNotFoundException;
import com.school.management.model.Sequence;
import com.school.management.dto.SequenceDTO;
import com.school.management.repository.SequenceRepository;

import jakarta.transaction.Transactional;

import com.school.management.mappers.SequenceMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SequenceService {
  private final SequenceRepository sequenceRepository;
  private final SequenceMapper mapper;

  
  @Transactional
  public SequenceDTO createSequence(SequenceDTO dto){
    Sequence entity = mapper.toEntity(dto);
    Sequence saved = sequenceRepository.save(entity);

    return mapper.toDto(saved);
    
  }

  @Transactional
  public SequenceDTO getSequenceById(Long id){
    return sequenceRepository.findById(id).map(mapper::toDto).orElseThrow(()-> new ResourceNotFoundException("Sequence non trouvee avec l'ID :" + id));
  }

  @Transactional
  public List<SequenceDTO> getALLSequence(){
    return sequenceRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
  }

  @Transactional
  public SequenceDTO updateSequence(Long id, SequenceDTO dto){
    Sequence existingSequence = sequenceRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Sequense non trouvee avec l'ID :" + id));

    
              //existingSequence.setTrimestre(dto.getTrimestre());
              existingSequence.setLibelleSequence(dto.getLibelleSequence());
              // entity.setTrimestre(existingSequence.getTrimeste());


              Sequence updated = sequenceRepository.save(existingSequence);

    return mapper.toDto(updated);
  }

  public void delete(Long id) {
    if (!sequenceRepository.existsById(id)) {
      throw new ResourceNotFoundException("trimestre non trouvee avec l'ID :" + id);
    }
    sequenceRepository.deleteById(id);
  }
  
  @Transactional
  public long getTotalSequence() {
    return sequenceRepository.count();
  }

}