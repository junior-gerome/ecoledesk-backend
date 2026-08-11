package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.AffectationCreateRequest;
import com.school.platform.academic.application.dto.AffectationDTO;
import com.school.platform.academic.application.dto.AffectationResponse;
import com.school.platform.academic.application.dto.AffectationUpdateRequest;
import java.util.List;

public interface AffectationService {
    List<AffectationDTO> getAllAffectations();
    List<AffectationResponse> getAllAffectationResponses();
    AffectationResponse getAffectationById(Long id);
    List<AffectationDTO> getAffectationsByEnseignant(Long enseignantId);
    List<AffectationResponse> getAffectationResponsesByTeacher(Long teacherId);
    List<AffectationDTO> getAffectationsByClasse(Long classeId);
    List<AffectationResponse> getAffectationResponsesByClasse(Long classeId);
    List<AffectationResponse> getAffectationResponsesBySchoolYear(Long schoolYearId);
    AffectationDTO createAffectation(AffectationDTO dto);
    AffectationResponse createAffectation(AffectationCreateRequest request);
    AffectationDTO updateAffectation(Long id, AffectationDTO dto);
    AffectationResponse updateAffectation(Long id, AffectationUpdateRequest request);
    void deleteAffectation(Long id);
}
