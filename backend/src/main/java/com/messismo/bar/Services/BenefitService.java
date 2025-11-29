package com.messismo.bar.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messismo.bar.DTOs.BenefitRequestDTO;
import com.messismo.bar.DTOs.BenefitResponseDTO;
import com.messismo.bar.Entities.Benefit;
import com.messismo.bar.Repositories.BenefitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BenefitService {

    @Autowired
    private BenefitRepository benefitRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Get all active benefits
    public List<BenefitResponseDTO> getAllActiveBenefits() {
        return benefitRepository.findByActiveTrue().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get benefit by ID
    public Optional<BenefitResponseDTO> getBenefitById(Long id) {
        return benefitRepository.findById(id)
                .filter(Benefit::getActive)
                .map(this::convertToResponseDTO);
    }

    // Create new benefit
    public BenefitResponseDTO createBenefit(BenefitRequestDTO requestDTO) {
        String currentUserEmail = getCurrentUserEmail();
        
        Benefit benefit = Benefit.builder()
                .type(requestDTO.getType())
                .pointsRequired(requestDTO.getPointsRequired())
                .discountType(requestDTO.getDiscountType())
                .discountValue(requestDTO.getDiscountValue())
                .applicableDays(listToJson(requestDTO.getApplicableDays()))
                .productIds(longListToJson(requestDTO.getProductIds()))
                .createdBy(currentUserEmail)
                .active(true)
                .build();
        
        Benefit savedBenefit = benefitRepository.save(benefit);
        return convertToResponseDTO(savedBenefit);
    }

    // Delete benefit (soft delete)
    public boolean deleteBenefit(Long id) {
        Optional<Benefit> benefitOpt = benefitRepository.findById(id);
        if (benefitOpt.isPresent() && benefitOpt.get().getActive()) {
            Benefit benefit = benefitOpt.get();
            benefit.setActive(false);
            benefitRepository.save(benefit);
            return true;
        }
        return false;
    }

    // Get benefits by type
    public List<BenefitResponseDTO> getBenefitsByType(Benefit.BenefitType type) {
        return benefitRepository.findByTypeAndActiveTrue(type).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get benefits available for specific points and current day
    public List<BenefitResponseDTO> getBenefitsForPoints(Integer points) {
        String currentDay = java.time.LocalDate.now().getDayOfWeek().name();
        
        List<Benefit> allBenefits = benefitRepository.findByPointsRequiredLessThanEqual(points);
        
        return allBenefits.stream()
                .filter(benefit -> benefit.isApplicableOnDay(currentDay))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper method to get current user email
    private String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    // Convert entity to response DTO
    private BenefitResponseDTO convertToResponseDTO(Benefit benefit) {
        return BenefitResponseDTO.builder()
                .id(benefit.getId())
                .type(benefit.getType())
                .pointsRequired(benefit.getPointsRequired())
                .discountType(benefit.getDiscountType())
                .discountValue(benefit.getDiscountValue())
                .applicableDays(jsonToStringList(benefit.getApplicableDays()))
                .productIds(jsonToLongList(benefit.getProductIds()))
                .createdBy(benefit.getCreatedBy())
                .createdAt(benefit.getCreatedAt())
                .active(benefit.getActive())
                .build();
    }

    // JSON conversion helper methods
    private String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String longListToJson(List<Long> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> jsonToStringList(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private List<Long> jsonToLongList(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}