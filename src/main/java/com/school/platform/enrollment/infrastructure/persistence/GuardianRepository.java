package com.school.platform.enrollment.infrastructure.persistence;

import com.school.platform.enrollment.domain.model.Guardian;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    Optional<Guardian> findByPersonId(Long personId);

    @Query("select g from Guardian g join g.person person where lower(person.email.value) = lower(:email)")
    Optional<Guardian> findByEmail(@Param("email") String email);

    @Query("select g from Guardian g join g.person person where person.phone.value = :phoneNumber")
    Optional<Guardian> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Recherche paginée (nom, prénom, email, téléphone) pour l'écran responsables.
     * Search optionnel ; countQuery séparé.
     */
    @Query(value = """
            select g from Guardian g join g.person person
            where (:search is null or :search = ''
                or lower(coalesce(person.firstName, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(person.lastName, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(person.email.value, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(person.phone.value, '')) like lower(concat('%', :search, '%')))
            """,
            countQuery = """
            select count(g) from Guardian g join g.person person
            where (:search is null or :search = ''
                or lower(coalesce(person.firstName, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(person.lastName, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(person.email.value, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(person.phone.value, '')) like lower(concat('%', :search, '%')))
            """)
    Page<Guardian> findWithSearch(@Param("search") String search, Pageable pageable);
}
