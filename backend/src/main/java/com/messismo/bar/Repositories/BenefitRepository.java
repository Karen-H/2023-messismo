package com.messismo.bar.Repositories;

import com.messismo.bar.Entities.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {
    
    // Find all active benefits
    List<Benefit> findByActiveTrue();
    
    // Find benefits by type
    List<Benefit> findByTypeAndActiveTrue(Benefit.BenefitType type);
    
    // Find benefits created by a specific user
    List<Benefit> findByCreatedByAndActiveTrue(String createdBy);
    
    // Find benefits that require specific points or less
    @Query("SELECT b FROM Benefit b WHERE b.pointsRequired <= ?1 AND b.active = true")
    List<Benefit> findByPointsRequiredLessThanEqual(Integer points);
}