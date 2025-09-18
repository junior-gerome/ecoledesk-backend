package com.school.management.repository;

import com.school.management.model.InscriptionStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface InscriptionStudentRepository extends JpaRepository<InscriptionStudent, Long> {
    Optional<InscriptionStudent> findByStudentIdAndAnneeScolaireId(Long studentId, Long anneeScolaireId);
    List<InscriptionStudent> findByClasseRoomIdAndAnneeScolaireId(Long classeRoomId, Long anneeScolaireId);

    List<InscriptionStudent> findByAnneeScolaireId(Long anneeScolaireId);

    Optional<InscriptionStudent> findByStudentIdAndClasseRoomId(Long studentId, Long classeRoomId);
    
    long countByClasseRoomId(Long ClasseRoomId);

    
    



}
