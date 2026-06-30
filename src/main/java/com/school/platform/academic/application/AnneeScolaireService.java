package com.school.platform.academic.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.AnneeScolaireDTO;
import com.school.platform.academic.application.mapper.AnneeScolaireMapper;
import com.school.platform.academic.domain.model.AnneeScolaire;
import com.school.platform.academic.infrastructure.persistence.AnneeScolaireRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnneeScolaireService {
  private final AnneeScolaireRepository anneeScolaireRepository;
  private final AnneeScolaireMapper mapper;

    @Transactional(readOnly = true)
    public List<AnneeScolaireDTO> getAllAnneeScolaire() {
      return anneeScolaireRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public AnneeScolaireDTO createAnneeScolaire(AnneeScolaireDTO dto) {
      AnneeScolaire entity = mapper.toEntity(dto);
      AnneeScolaire saved = anneeScolaireRepository.save(entity);
      return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public AnneeScolaireDTO getById(Long id) {
      return anneeScolaireRepository.findById(id)
          .map(mapper::toDto)
          .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", "id", id));
    }

    @Transactional
    public AnneeScolaireDTO updateAnneeScolaire(Long id, AnneeScolaireDTO dto) {
      AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", "id", id));

      anneeScolaire.setLibelleAnneeScolaire(dto.getLibelleAnneeScolaire());
      anneeScolaire.setDateDebut(dto.getDateDebut());
      anneeScolaire.setDateFin(dto.getDateFin());
      anneeScolaire.setStatutCode(dto.isStatutCode());
      return mapper.toDto(anneeScolaireRepository.save(anneeScolaire));
    }

    @Transactional
    public void deleteAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", "id", id));
      anneeScolaireRepository.delete(anneeScolaire);
    }

    @Transactional(readOnly = true)
    public AnneeScolaireDTO getActiveAnneeScolaire() {
      return anneeScolaireRepository.findByStatutCode(true).map(mapper::toDto)
          .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", "statutCode", true));
    }

    @Transactional
    public AnneeScolaire activateAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaireToActivate = anneeScolaireRepository.findByIdForUpdate(id)
          .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", "id", id));

      anneeScolaireRepository.deactivateActiveYears();
      anneeScolaireToActivate.setStatutCode(true);
      return anneeScolaireRepository.save(anneeScolaireToActivate);
    }

    @Transactional(readOnly = true)
    public boolean existsByStatutCode(boolean statutCode) {
      return anneeScolaireRepository.findByStatutCode(statutCode).isPresent();
    }

    @Transactional
    public AnneeScolaire inactivateAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaireToInactivate = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", "id", id));

      anneeScolaireToInactivate.setStatutCode(false);
      return anneeScolaireRepository.save(anneeScolaireToInactivate);
    }
}
