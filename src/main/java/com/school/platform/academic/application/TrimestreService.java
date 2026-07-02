package com.school.platform.academic.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.domain.model.Trimestre;
import com.school.platform.academic.application.dto.TrimestreDTO;
import com.school.platform.academic.infrastructure.persistence.TrimestreRepository;

import jakarta.transaction.Transactional;

import com.school.platform.academic.application.mapper.TrimestreMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrimestreService{
  private final TrimestreRepository trimestreRepository;
  private final TrimestreMapper mapper;


  @Transactional
  public TrimestreDTO createTrimestre(TrimestreDTO dto){
    Trimestre entity = mapper.toEntity(dto);
    Trimestre saved = trimestreRepository.save(entity);

    return mapper.toDto(saved);
    
  }

  @Transactional
  public TrimestreDTO getTrimestreById(Long id){
    return trimestreRepository.findById(id).map(mapper::toDto).orElseThrow(()-> new ResourceNotFoundException("Trimestre non trouve avec l'ID :" + id));
  }

  @Transactional
  public List<TrimestreDTO> getALLTrimestre(){
    return trimestreRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
  }

  @Transactional
  public TrimestreDTO updateTrimestre(Long id, TrimestreDTO dto){
    Trimestre existingTrimestre = trimestreRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Trimestre non trouve avec l'ID :" + id));

    // Trimestre entity = mapper.toEntity(dto);
    //           existingTrimestre.setId(dto.getId());
              existingTrimestre.setLibelleTrimestre(dto.getLibelleTrimestre());
              // entity.setTypeTrimestre(existingTrimestre.getTypeTrimeste());


              Trimestre updated = trimestreRepository.save(existingTrimestre);

    return mapper.toDto(updated);
  }

  public void delete(Long id) {
    if (!trimestreRepository.existsById(id)) {
      throw new ResourceNotFoundException("trimestre non trouvee avec l'ID :" + id);
    }
    trimestreRepository.deleteById(id);
  }
  
  @Transactional
  public long getTotalTrimestre() {
    return trimestreRepository.count();
  }

}