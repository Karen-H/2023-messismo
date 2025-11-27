package com.messismo.bar.DTOs;

import com.messismo.bar.Entities.Benefit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitResponseDTO {
    
    private Long id;
    private Benefit.BenefitType type;
    private Integer pointsRequired;
    
    // Fields for DISCOUNT type
    private Benefit.DiscountType discountType;
    private Double discountValue;
    private List<String> applicableDays;
    
    // Fields for FREE_PRODUCT type
    private List<Long> productIds;
    
    // Audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private Boolean active;
    
    // Helper method to get display text for the benefit
    public String getDisplayText() {
        StringBuilder text = new StringBuilder();
        text.append(pointsRequired).append(" points for ");
        
        if (type == Benefit.BenefitType.DISCOUNT) {
            if (discountType == Benefit.DiscountType.PERCENTAGE) {
                text.append(discountValue).append("% discount");
            } else {
                text.append("$").append(discountValue).append(" discount");
            }
        } else {
            text.append("free product");
            if (productIds != null && !productIds.isEmpty()) {
                text.append(" (").append(productIds.size()).append(" products)");
            }
        }
        
        return text.toString();
    }
}