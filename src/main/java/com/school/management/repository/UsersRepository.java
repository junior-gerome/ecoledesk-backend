package com.school.management.repository;

import java.util.List;
//import java.util.Optional;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;

import com.school.management.model.Users;
import com.school.management.model.UsersProfil;


@Repository
@EnableRedisRepositories
public interface UsersRepository extends JpaRepository<Users, Long> {
    
    Optional<Users> findByUsername(String username);
    List<Users> findByProfils(List<UsersProfil> profils);

    List<Users> findAll();

    List<Users> findAllById(Iterable<Long> ids) ;
    boolean existsByUsername(String username);

    List<Users> findByActifTrue();
   // Optional<User> findByNameUser(String name);
}
