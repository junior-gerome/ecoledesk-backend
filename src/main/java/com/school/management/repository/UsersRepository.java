package com.school.management.repository;

import java.util.List;
//import java.util.Optional;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;

import com.school.management.model.Users;
import com.school.management.model.UsersProfil;

//import io.lettuce.core.dynamic.annotation.Param;


@Repository
@EnableRedisRepositories
public interface UsersRepository extends JpaRepository<Users, Long> {
       
     /**
     * Retourne l'utilisateur identifié par username ET charge la collection "profils"
     * dans la même opération (évite LazyInitializationException).
     */
   
    @EntityGraph(attributePaths = {"profils"})

    Optional<Users> findByUsername(String username);

    List<Users> findByProfils(List<UsersProfil> profils);

    // @Query("select u from users u left join fetch u.profils p where u.username = :username")
    // Optional<Users> findByUsernameWithProfils(@Param("username") String username);

    List<Users> findAll();

    List<Users> findAllById(Iterable<Long> ids) ;
    boolean existsByUsername(String username);

    List<Users> findByActifTrue();
   // Optional<User> findByNameUser(String name);
}
