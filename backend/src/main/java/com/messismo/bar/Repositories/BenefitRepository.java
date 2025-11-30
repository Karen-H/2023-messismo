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
    
    // Find duplicate benefits (exact same configuration)
    @Query("SELECT b FROM Benefit b WHERE b.type = ?1 AND b.pointsRequired = ?2 AND " +
           "((?3 IS NULL AND b.discountType IS NULL) OR b.discountType = ?3) AND " +
           "((?4 IS NULL AND b.discountValue IS NULL) OR b.discountValue = ?4) AND " +
           "b.applicableDays = ?5 AND " +
           "((?6 IS NULL AND b.productIds IS NULL) OR b.productIds = ?6) AND " +
           "b.active = true")
    List<Benefit> findDuplicateBenefits(Benefit.BenefitType type, Integer pointsRequired, Benefit.DiscountType discountType, Double discountValue, String applicableDays, String productIds);
}