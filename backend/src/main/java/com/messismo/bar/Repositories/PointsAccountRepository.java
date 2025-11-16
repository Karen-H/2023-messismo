package com.messismo.bar.Repositories;

import com.messismo.bar.Entities.PointsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointsAccountRepository extends JpaRepository<PointsAccount, Long> {
    
    Optional<PointsAccount> findByClientId(String clientId);
    
    boolean existsByClientId(String clientId);
}