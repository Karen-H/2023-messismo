package com.messismo.bar.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "benefits")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Benefit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType type; // DISCOUNT, FREE_PRODUCT
    
    @Column(nullable = false)
    private Integer pointsRequired;
    
    // Fields for DISCOUNT type
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT
    
    private Double discountValue;
    
    // Days of the week when benefit applies (JSON array)
    @Column(columnDefinition = "TEXT")
    private String applicableDays; // JSON: ["MONDAY", "TUESDAY"] or ["EVERYDAY"]
    
    // Fields for FREE_PRODUCT type
    // Product IDs (JSON array)
    @Column(columnDefinition = "TEXT")
    private String productIds; // JSON: [1, 2, 3]
    
    // Audit fields
    @Column(nullable = false)
    private String createdBy; // Email of the user who created the benefit
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Boolean active = true; // Soft delete flag
    
    // Helper method to check if benefit is applicable on a specific day
    public boolean isApplicableOnDay(String day) {
        if (applicableDays == null || applicableDays.isEmpty()) {
            return false;
        }
        return applicableDays.contains("EVERYDAY") || applicableDays.contains(day.toUpperCase());
    }
    
    public enum BenefitType {
        DISCOUNT,
        FREE_PRODUCT
    }
    
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
    

}