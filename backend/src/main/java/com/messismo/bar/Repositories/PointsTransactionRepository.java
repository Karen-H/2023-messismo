package com.messismo.bar.Repositories;

import com.messismo.bar.Entities.PointsTransaction;
import com.messismo.bar.Entities.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    
    List<PointsTransaction> findByClientIdOrderByCreatedAtDesc(String clientId);
    
    List<PointsTransaction> findByClientIdAndTypeOrderByCreatedAtDesc(String clientId, TransactionType type);
    
    @Query("SELECT SUM(pt.amount) FROM PointsTransaction pt WHERE pt.clientId = :clientId AND pt.type = :type")
    Double getTotalAmountByClientIdAndType(@Param("clientId") String clientId, @Param("type") TransactionType type);
}