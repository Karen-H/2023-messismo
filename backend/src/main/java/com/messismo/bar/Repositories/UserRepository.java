package com.messismo.bar.Repositories;

import com.messismo.bar.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    
    @Query("SELECT u.clientId FROM User u WHERE u.clientId IS NOT NULL")
    Set<String> findAllClientIds();
    
    Optional<User> findByClientId(String clientId);
}
