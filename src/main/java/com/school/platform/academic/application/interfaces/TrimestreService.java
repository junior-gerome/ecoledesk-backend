package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.TrimestreDTO;
import java.util.List;

public interface TrimestreService {
    TrimestreDTO createTrimestre(TrimestreDTO dto);
    TrimestreDTO getTrimestreById(Long id);
    List<TrimestreDTO> getALLTrimestre();
    TrimestreDTO updateTrimestre(Long id, TrimestreDTO dto);
    void delete(Long id);
    long getTotalTrimestre();
}
