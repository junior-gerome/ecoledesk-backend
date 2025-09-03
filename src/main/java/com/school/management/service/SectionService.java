package com.school.management.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.school.exception.ResourceNotFoundException;
import com.school.management.model.Section;
import com.school.management.repository.SectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {
  private final SectionRepository sectionRepository;

  public Section createSection(Section section) {
    return sectionRepository.save(section);
  }

  public Section getSectionById(Long id) {
    return sectionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
  }

  public Section getSectionByLibelle(String libelle) {
    return sectionRepository.findByLibelle(libelle)
        .orElseThrow(() -> new ResourceNotFoundException("Section not found with libelle: " + libelle));
  }
  
  public Section updateSection(Long id, Section updatedSection) {
    Section existingSection = getSectionById(id);
    existingSection.setLibelle(updatedSection.getLibelle());
    existingSection.setDescription(updatedSection.getDescription());
    return sectionRepository.save(existingSection);
  } 

  public void deleteSection(Long id) {
    Section existingSection = getSectionById(id);
    sectionRepository.delete(existingSection);
  }

  public List<Section> getAllSections() {
    return sectionRepository.findAll();
  }

}
