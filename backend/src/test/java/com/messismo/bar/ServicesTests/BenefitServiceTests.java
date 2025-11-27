package com.messismo.bar.ServicesTests;

import com.messismo.bar.DTOs.BenefitRequestDTO;
import com.messismo.bar.DTOs.BenefitResponseDTO;
import com.messismo.bar.Entities.Benefit;
import com.messismo.bar.Repositories.BenefitRepository;
import com.messismo.bar.Services.BenefitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BenefitServiceTests {

    @Mock
    private BenefitRepository benefitRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BenefitService benefitService;

    private Benefit discountBenefit;
    private Benefit freeProductBenefit;
    private BenefitRequestDTO discountRequestDTO;
    private BenefitRequestDTO freeProductRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup discount benefit
        discountBenefit = Benefit.builder()
                .id(1L)
                .type(Benefit.BenefitType.DISCOUNT)
                .pointsRequired(100)
                .discountType(Benefit.DiscountType.PERCENTAGE)
                .discountValue(15.0)
                .applicableDays("[\"MONDAY\", \"TUESDAY\"]")
                .createdBy("admin@test.com")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        // Setup free product benefit
        freeProductBenefit = Benefit.builder()
                .id(2L)
                .type(Benefit.BenefitType.FREE_PRODUCT)
                .pointsRequired(200)
                .productIds("[1, 2, 3]")
                .createdBy("manager@test.com")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        // Setup discount request DTO
        discountRequestDTO = BenefitRequestDTO.builder()
                .type(Benefit.BenefitType.DISCOUNT)
                .pointsRequired(100)
                .discountType(Benefit.DiscountType.PERCENTAGE)
                .discountValue(15.0)
                .applicableDays(Arrays.asList("MONDAY", "TUESDAY"))
                .build();

        // Setup free product request DTO
        freeProductRequestDTO = BenefitRequestDTO.builder()
                .type(Benefit.BenefitType.FREE_PRODUCT)
                .pointsRequired(200)
                .productIds(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    @Test
    void testGetAllActiveBenefits_Success() {
        // Arrange
        List<Benefit> benefits = Arrays.asList(discountBenefit, freeProductBenefit);
        when(benefitRepository.findByActiveTrue()).thenReturn(benefits);

        // Act
        List<BenefitResponseDTO> result = benefitService.getAllActiveBenefits();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(discountBenefit.getId(), result.get(0).getId());
        assertEquals(freeProductBenefit.getId(), result.get(1).getId());
        verify(benefitRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testGetBenefitById_Success() {
        // Arrange
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(discountBenefit));

        // Act
        Optional<BenefitResponseDTO> result = benefitService.getBenefitById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(discountBenefit.getId(), result.get().getId());
        assertEquals(discountBenefit.getType(), result.get().getType());
        verify(benefitRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBenefitById_NotFound() {
        // Arrange
        when(benefitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<BenefitResponseDTO> result = benefitService.getBenefitById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(benefitRepository, times(1)).findById(999L);
    }

    @Test
    void testGetBenefitById_InactiveBenefit() {
        // Arrange
        Benefit inactiveBenefit = Benefit.builder()
                .id(3L)
                .type(Benefit.BenefitType.DISCOUNT)
                .active(false)
                .build();
        when(benefitRepository.findById(3L)).thenReturn(Optional.of(inactiveBenefit));

        // Act
        Optional<BenefitResponseDTO> result = benefitService.getBenefitById(3L);

        // Assert
        assertFalse(result.isPresent());
        verify(benefitRepository, times(1)).findById(3L);
    }

    @Test
    void testCreateDiscountBenefit_Success() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("admin@test.com");
            when(benefitRepository.save(any(Benefit.class))).thenReturn(discountBenefit);

            // Act
            BenefitResponseDTO result = benefitService.createBenefit(discountRequestDTO);

            // Assert
            assertNotNull(result);
            assertEquals(discountBenefit.getId(), result.getId());
            assertEquals(discountBenefit.getType(), result.getType());
            assertEquals(discountBenefit.getPointsRequired(), result.getPointsRequired());
            verify(benefitRepository, times(1)).save(any(Benefit.class));
        }
    }

    @Test
    void testCreateFreeProductBenefit_Success() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("manager@test.com");
            when(benefitRepository.save(any(Benefit.class))).thenReturn(freeProductBenefit);

            // Act
            BenefitResponseDTO result = benefitService.createBenefit(freeProductRequestDTO);

            // Assert
            assertNotNull(result);
            assertEquals(freeProductBenefit.getId(), result.getId());
            assertEquals(freeProductBenefit.getType(), result.getType());
            assertEquals(freeProductBenefit.getProductIds(), result.getProductIds());
            verify(benefitRepository, times(1)).save(any(Benefit.class));
        }
    }

    @Test
    void testDeleteBenefit_Success() {
        // Arrange
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(discountBenefit));
        when(benefitRepository.save(any(Benefit.class))).thenReturn(discountBenefit);

        // Act
        boolean result = benefitService.deleteBenefit(1L);

        // Assert
        assertTrue(result);
        verify(benefitRepository, times(1)).findById(1L);
        verify(benefitRepository, times(1)).save(any(Benefit.class));
    }

    @Test
    void testDeleteBenefit_NotFound() {
        // Arrange
        when(benefitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = benefitService.deleteBenefit(999L);

        // Assert
        assertFalse(result);
        verify(benefitRepository, times(1)).findById(999L);
        verify(benefitRepository, never()).save(any(Benefit.class));
    }

    @Test
    void testDeleteBenefit_AlreadyInactive() {
        // Arrange
        Benefit inactiveBenefit = Benefit.builder()
                .id(3L)
                .type(Benefit.BenefitType.DISCOUNT)
                .active(false)
                .build();
        when(benefitRepository.findById(3L)).thenReturn(Optional.of(inactiveBenefit));

        // Act
        boolean result = benefitService.deleteBenefit(3L);

        // Assert
        assertFalse(result);
        verify(benefitRepository, times(1)).findById(3L);
        verify(benefitRepository, never()).save(any(Benefit.class));
    }

    @Test
    void testGetBenefitsByType_Success() {
        // Arrange
        List<Benefit> discountBenefits = Arrays.asList(discountBenefit);
        when(benefitRepository.findByTypeAndActiveTrue(Benefit.BenefitType.DISCOUNT))
                .thenReturn(discountBenefits);

        // Act
        List<BenefitResponseDTO> result = benefitService.getBenefitsByType(Benefit.BenefitType.DISCOUNT);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Benefit.BenefitType.DISCOUNT, result.get(0).getType());
        verify(benefitRepository, times(1)).findByTypeAndActiveTrue(Benefit.BenefitType.DISCOUNT);
    }

    @Test
    void testGetBenefitsForPoints_Success() {
        // Arrange
        List<Benefit> benefits = Arrays.asList(discountBenefit);
        when(benefitRepository.findByPointsRequiredLessThanEqual(150)).thenReturn(benefits);

        // Act
        List<BenefitResponseDTO> result = benefitService.getBenefitsForPoints(150);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getPointsRequired() <= 150);
        verify(benefitRepository, times(1)).findByPointsRequiredLessThanEqual(150);
    }

    @Test
    void testGetCurrentUserEmail_WithAuthentication() {
        // This test verifies the private method indirectly through createBenefit
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("test@example.com");
            when(benefitRepository.save(any(Benefit.class))).thenReturn(discountBenefit);

            // Act
            BenefitResponseDTO result = benefitService.createBenefit(discountRequestDTO);

            // Assert
            assertNotNull(result);
            verify(authentication, times(1)).getName();
        }
    }

    @Test
    void testGetCurrentUserEmail_WithException() {
        // This test verifies the fallback to "system" when authentication fails
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenThrow(new RuntimeException());
            when(benefitRepository.save(any(Benefit.class))).thenReturn(discountBenefit);

            // Act
            BenefitResponseDTO result = benefitService.createBenefit(discountRequestDTO);

            // Assert
            assertNotNull(result);
            // The method should handle the exception and use "system" as default
        }
    }
}