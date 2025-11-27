package com.messismo.bar.DTOs;

import com.messismo.bar.Entities.Benefit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitRequestDTO {
    
    private Benefit.BenefitType type; // DISCOUNT, FREE_PRODUCT
    
    private Integer pointsRequired;
    
    // Fields for DISCOUNT type
    private Benefit.DiscountType discountType; // PERCENTAGE, FIXED_AMOUNT
    private Double discountValue;
    private List<String> applicableDays; // ["MONDAY", "TUESDAY"] or ["EVERYDAY"]
    
    // Fields for FREE_PRODUCT type
    private List<Long> productIds; // [1, 2, 3]
}