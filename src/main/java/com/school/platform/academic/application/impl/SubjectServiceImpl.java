//package com.school.platform.academic.application.impl;
//
//import org.springframework.stereotype.Service;
//
//import java.text.Normalizer;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import com.school.platform.shared.domain.exception.ResourceNotFoundException;
//import com.school.platform.academic.application.dto.subject.SubjectDTO;
//import com.school.platform.academic.application.interfaces.SubjectService;
//import com.school.platform.academic.application.reponseMapper.SubjectMapper;
//import com.school.platform.academic.domain.model.Subject;
//import com.school.platform.academic.infrastructure.persistence.SubjectRepository;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class SubjectServiceImpl implements SubjectService {
//  private final SubjectRepository subjectRepository;
//  private final SubjectMapper reponseMapper;
//
//  @Transactional
//  public SubjectDTO createdSubject(SubjectDTO dto) {
//    Subject entity = reponseMapper.toEntity(dto);
//    Subject saved = subjectRepository.save(entity);
//    return reponseMapper.toDto(saved);
//  }
//
//  @Transactional
//  public SubjectDTO updatedSubject(Long id, SubjectDTO dto) {
//    Subject subject = subjectRepository.findById(id)
//        .orElseThrow(() -> new ResourceNotFoundException("Matier non trouver par ID:" + id));
//
//            subject.setNameSubject(dto.getNameSubject());
//            subject.setCoefficient(dto.getCoefficient());
//            subject.setCode(dto.getCode());
//            subject.setDescription(dto.getDescription());
//
//    Subject updated = subjectRepository.save(subject);
//
//    return reponseMapper.toDto(updated);
//  }
//
//  @Transactional
//  public List<SubjectDTO> getAllSubject(){
//    return subjectRepository.findAll().stream().map(reponseMapper::toDto).collect(Collectors.toList());
//  }
//
//  @Transactional
//  public long getTotalSubjects(){
//    return subjectRepository.count();
//  }
//
//  @Transactional
//  public void deleteSubject(Long id){
//    if(!subjectRepository.existsById(id)){
//      throw new ResourceNotFoundException("matiere non trouvee avec l'id:" + id);
//    }
//    subjectRepository.deleteById(id);
//  }
//
//  @Transactional
//  public SubjectDTO getSubjectById(Long id){
//    return subjectRepository.findById(id).map(reponseMapper::toDto).orElseThrow(()-> new ResourceNotFoundException("Matiere", "id", id));
//  }
//
//  @Override
//  @Transactional(readOnly = true)
//  public String generateSubjectCode(String nameSubject) {
//
//    if (nameSubject == null || nameSubject.isBlank()) {
//      throw new IllegalArgumentException(
//              "Le nom de la matière est obligatoire"
//      );
//    }
//
//    String normalized = Normalizer
//            .normalize(nameSubject, Normalizer.Form.NFD)
//            .replaceAll("\\p{M}", "")
//            .toUpperCase()
//            .replaceAll("[^A-Z0-9 ]", "")
//            .trim();
//
//    Set<String> ignoredWords = Set.of(
//            "ET",
//            "DE",
//            "DU",
//            "DES",
//            "LA",
//            "LE",
//            "LES",
//            "EN",
//            "A",
//            "AU",
//            "AUX"
//    );
//
//    String[] words = normalized.split("\\s+");
//
//    StringBuilder codeBuilder = new StringBuilder();
//
//    for (String word : words) {
//
//      if (!ignoredWords.contains(word) && !word.isBlank()) {
//        codeBuilder.append(word.charAt(0));
//      }
//    }
//
//    String code = codeBuilder.toString();
//
//    // Si le code est trop court, utiliser les premières lettres du nom
//    if (code.length() < 3) {
//      code = normalized
//              .replaceAll("\\s+", "")
//              .substring(
//                      0,
//                      Math.min(4, normalized.replaceAll("\\s+", "").length())
//              );
//    }
//
//    // Limite de la colonne
//    if (code.length() > 20) {
//      code = code.substring(0, 20);
//    }
//
//    String baseCode = code;
//    int counter = 1;
//
//    while (subjectRepository.existsByCode(code)) {
//      code = baseCode + counter;
//      counter++;
//    }
//
//    return code;
//  }
//
//}


package com.school.platform.academic.application.impl;

import com.school.platform.academic.application.dto.subject.SubjectReponse;
import com.school.platform.academic.application.dto.subject.SubjectRequest;
import com.school.platform.academic.application.interfaces.SubjectService;
import com.school.platform.academic.application.mapper.SubjectReponseMapper;
import com.school.platform.academic.application.mapper.SubjectRequestMapper;
import com.school.platform.academic.domain.model.Subject;
import com.school.platform.academic.infrastructure.persistence.SubjectRepository;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

  private final SubjectRepository subjectRepository;
  private final SubjectReponseMapper reponseMapper;
  private final SubjectRequestMapper requestMapper;

  /**
   * Création d'une matière.
   * Le code est généré par le backend.
   */
  @Override
  @Transactional
  public SubjectRequest createdSubject(SubjectRequest dto) {

    if (dto.getNameSubject() == null || dto.getNameSubject().isBlank()) {
      throw new IllegalArgumentException(
              "Le nom de la matière est obligatoire"
      );
    }

    Subject entity = requestMapper.toEntity(dto);

    // Le backend génère le code
    String generatedCode = generateSubjectCode(dto.getNameSubject());

    entity.setCode(generatedCode);

    Subject saved = subjectRepository.save(entity);

    return requestMapper.toDto(saved);
  }

  /**
   * Modification d'une matière.
   * <p>
   * Le code existant est conservé.
   */
  @Override
  @Transactional
  public SubjectReponse updatedSubject(Long id, SubjectReponse dto) {

    Subject subject = subjectRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Matiere",
                            "id",
                            id
                    )
            );

    subject.setNameSubject(dto.getNameSubject());
    subject.setCoefficient(dto.getCoefficient());
    subject.setDescription(dto.getDescription());

    /*
     * On NE modifie pas le code.
     *
     * Le code appartient à la matière et peut être utilisé
     * ailleurs dans l'application.
     */

    Subject updated = subjectRepository.save(subject);

    return reponseMapper.toDto(updated);
  }

  /**
   * Récupérer toutes les matières.
   */
  @Override
  @Transactional(readOnly = true)
  public List<SubjectReponse> getAllSubject() {

    return subjectRepository.findAll()
            .stream()
            .map(reponseMapper::toDto)
            .collect(Collectors.toList());
  }

  /**
   * Compter les matières.
   */
  @Override
  @Transactional(readOnly = true)
  public long getTotalSubjects() {

    return subjectRepository.count();
  }

  /**
   * Supprimer une matière.
   */
  @Override
  @Transactional
  public void deleteSubject(Long id) {

    if (!subjectRepository.existsById(id)) {
      throw new ResourceNotFoundException(
              "Matiere",
              "id",
              id
      );
    }

    subjectRepository.deleteById(id);
  }

  /**
   * Récupérer une matière par son ID.
   */
  @Override
  @Transactional(readOnly = true)
  public SubjectReponse getSubjectById(Long id) {

    return subjectRepository.findById(id)
            .map(reponseMapper::toDto)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Matiere",
                            "id",
                            id
                    )
            );
  }

  /**
   * Générer automatiquement un code pour une matière.
   *
   * Exemple :
   *
   * Mathématiques              -> M
   * Français                   -> F
   * Sciences de la Vie        -> SV
   * Éducation Physique        -> EP
   * Mathématiques et Français -> MF
   *
   * Les mots inutiles sont ignorés.
   */
  @Override
  @Transactional(readOnly = true)
  public String generateSubjectCode(String nameSubject) {

    if (nameSubject == null || nameSubject.isBlank()) {
      throw new IllegalArgumentException(
              "Le nom de la matière est obligatoire"
      );
    }

    /*
     * 1. Normalisation du nom
     */
    String normalized = Normalizer
            .normalize(nameSubject, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase()
            .replaceAll("[^A-Z0-9 ]", "")
            .trim();

    /*
     * 2. Mots à ignorer
     */
    Set<String> ignoredWords = Set.of(
            "ET",
            "DE",
            "DU",
            "DES",
            "LA",
            "LE",
            "LES",
            "EN",
            "A",
            "AU",
            "AUX"
    );

    /*
     * 3. Découpage du nom en mots
     */
    String[] words = normalized.split("\\s+");

    StringBuilder codeBuilder = new StringBuilder();

    /*
     * 4. Prendre la première lettre
     *    de chaque mot important
     */
    for (String word : words) {

      if (!word.isBlank() && !ignoredWords.contains(word)) {

        codeBuilder.append(word.charAt(0));
      }
    }

    String code = codeBuilder.toString();

    /*
     * 5. Si le code contient moins de 3 caractères,
     *    prendre les premières lettres du nom complet.
     */
    if (code.length() < 3) {

      String cleanName = normalized
              .replaceAll("\\s+", "");

      code = cleanName.substring(
              0,
              Math.min(4, cleanName.length())
      );
    }

    /*
     * 6. Maximum 20 caractères
     */
    if (code.length() > 20) {

      code = code.substring(0, 20);
    }

    /*
     * 7. Vérifier que le code n'existe pas déjà.
     *
     * Exemple :
     *
     * MATH existe
     *
     * alors :
     * MATH1
     *
     * si MATH1 existe :
     * MATH2
     */
    String baseCode = code;
    int counter = 1;

    while (subjectRepository.existsByCode(code)) {

      code = baseCode + counter;
      counter++;
    }

    return code;
  }
}
