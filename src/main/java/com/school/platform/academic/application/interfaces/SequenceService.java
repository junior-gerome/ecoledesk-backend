package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.sequence.SequenceDTO;
import java.util.List;

public interface SequenceService {
    SequenceDTO createSequence(SequenceDTO dto);
    SequenceDTO getSequenceById(Long id);
    List<SequenceDTO> getALLSequence();
    SequenceDTO updateSequence(Long id, SequenceDTO dto);
    void delete(Long id);
    long getTotalSequence();
}
