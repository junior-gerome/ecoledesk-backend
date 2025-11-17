package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.SectionDTO;
import com.school.management.mappers.SectionMapper;
import com.school.management.model.Section;
import com.school.management.repository.SectionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SectionService {
  private final SectionRepository sectionRepository;
  private final SectionMapper mapper;

  
  public SectionDTO createSection(SectionDTO dto) {
    Section entity = mapper.toEntity(dto);
    Section saved = sectionRepository.save(entity);

    return mapper.toDto(saved);
  }

  public SectionDTO getSectionById(Long id) {
    Section entity = sectionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
    return mapper.toDto(entity);
  }

  public SectionDTO getSectionByLibelle(String libelle) {

    Section entity = sectionRepository.findByLibelle(libelle)
        .orElseThrow(() -> new ResourceNotFoundException("Section not found with libelle: " + libelle));
    return mapper.toDto(entity);
  }
  
  
  public SectionDTO updateSection(Long id, SectionDTO dto) {
    Section existingSection = sectionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Section non trouver par ID:" + id));
    
    existingSection.setLibelle(dto.getLibelle());
    existingSection.setDescription(dto.getDescription());
    return mapper.toDto(existingSection);
  } 

  public void deleteSection(Long id) {
    if (!sectionRepository.existsById(id)) {
      throw new ResourceNotFoundException("Section non trouver par ID:" + id);}
    sectionRepository.deleteById(id);
  }

  public List<SectionDTO> getAllSections() {
    return sectionRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
  }

  public long getTotalSections() {
    return sectionRepository.count();
  }

}
