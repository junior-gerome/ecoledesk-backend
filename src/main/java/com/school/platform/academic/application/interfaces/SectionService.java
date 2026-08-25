package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.section.SectionDTO;
import java.util.List;

public interface SectionService {
    SectionDTO createSection(SectionDTO dto);
    SectionDTO getSectionById(Long id);
    SectionDTO getSectionByLibelle(String libelle);
    SectionDTO updateSection(Long id, SectionDTO dto);
    void deleteSection(Long id);
    List<SectionDTO> getAllSections();
    long getTotalSections();
}
