package com.school.management.repository;

import com.school.management.model.AnneeScolaire;
import com.school.management.model.ClasseRoom;
import com.school.management.model.InscriptionStudent;
import com.school.management.model.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface InscriptionStudentRepository extends JpaRepository<InscriptionStudent, Long> {

    Optional<InscriptionStudent> findByStudentIdAndAnneeScolaireId(Long studentId, Long anneeScolaireId);

    Optional<InscriptionStudent> findByStudentIdAndClasseRoomId(Long studentId, Long classeRoomId);

    List<InscriptionStudent> findByClasseRoomIdAndAnneeScolaireId(Long classeRoomId, Long anneeScolaireId);

    List<InscriptionStudent> findByAnneeScolaireId(Long anneeScolaireId);

    List<InscriptionStudent> findByClasseRoom(ClasseRoom classeRoom);

    List<InscriptionStudent> findByClasseRoomId(Long classeRoomId);

    long countByClasseRoom(ClasseRoom classeRoom);

    long countByStudent(Student student);

    
    long countByClasseRoomId(Long classeRoomId);

    long countByAnneeScolaireId(Long anneeScolaireId);
    

    long countByStudentId(Long studentId);

     // Nouvelle méthode pour compter les étudiants pour une classe et une année scolaire
    long countByClasseRoomAndAnneeScolaire(ClasseRoom classeRoom, AnneeScolaire anneeScolaire);
    

    long countByClasseRoomIdAndAnneeScolaireId(Long classeRoomId, Long anneeScolaireId);
    

    
    



}
