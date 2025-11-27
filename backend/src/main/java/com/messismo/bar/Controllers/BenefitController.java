package com.messismo.bar.Controllers;

import com.messismo.bar.DTOs.BenefitRequestDTO;
import com.messismo.bar.DTOs.BenefitResponseDTO;
import com.messismo.bar.Entities.Benefit;
import com.messismo.bar.Services.BenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/benefits")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BenefitController {

    @Autowired
    private BenefitService benefitService;

    // Get all benefits (accessible to all authenticated users)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VALIDATEDEMPLOYEE') or hasRole('CLIENT')")
    public ResponseEntity<List<BenefitResponseDTO>> getAllBenefits() {
        List<BenefitResponseDTO> benefits = benefitService.getAllActiveBenefits();
        return ResponseEntity.ok(benefits);
    }

    // Get benefit by ID (accessible to all authenticated users)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VALIDATEDEMPLOYEE') or hasRole('CLIENT')")
    public ResponseEntity<BenefitResponseDTO> getBenefitById(@PathVariable Long id) {
        Optional<BenefitResponseDTO> benefit = benefitService.getBenefitById(id);
        return benefit.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // Create new benefit (only ADMIN and MANAGER)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BenefitResponseDTO> createBenefit(@RequestBody BenefitRequestDTO requestDTO) {
        try {
            // Basic validation
            if (requestDTO.getPointsRequired() == null || requestDTO.getPointsRequired() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            if (requestDTO.getType() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Validate DISCOUNT type fields
            if (requestDTO.getType() == Benefit.BenefitType.DISCOUNT) {
                if (requestDTO.getDiscountType() == null || requestDTO.getDiscountValue() == null || 
                    requestDTO.getDiscountValue() <= 0 || requestDTO.getApplicableDays() == null || 
                    requestDTO.getApplicableDays().isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Validate FREE_PRODUCT type fields
            if (requestDTO.getType() == Benefit.BenefitType.FREE_PRODUCT) {
                if (requestDTO.getProductIds() == null || requestDTO.getProductIds().isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            BenefitResponseDTO createdBenefit = benefitService.createBenefit(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBenefit);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete benefit (only ADMIN and MANAGER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteBenefit(@PathVariable Long id) {
        boolean deleted = benefitService.deleteBenefit(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Get benefits by type (accessible to all authenticated users)
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VALIDATEDEMPLOYEE') or hasRole('CLIENT')")
    public ResponseEntity<List<BenefitResponseDTO>> getBenefitsByType(@PathVariable Benefit.BenefitType type) {
        List<BenefitResponseDTO> benefits = benefitService.getBenefitsByType(type);
        return ResponseEntity.ok(benefits);
    }

    // Get benefits available for specific points (accessible to all authenticated users)
    @GetMapping("/available/{points}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VALIDATEDEMPLOYEE') or hasRole('CLIENT')")
    public ResponseEntity<List<BenefitResponseDTO>> getBenefitsForPoints(@PathVariable Integer points) {
        List<BenefitResponseDTO> benefits = benefitService.getBenefitsForPoints(points);
        return ResponseEntity.ok(benefits);
    }
}