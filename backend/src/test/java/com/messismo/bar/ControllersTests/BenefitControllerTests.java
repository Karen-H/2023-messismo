package com.messismo.bar.ControllersTests;

import com.messismo.bar.Controllers.BenefitController;
import com.messismo.bar.DTOs.BenefitRequestDTO;
import com.messismo.bar.DTOs.BenefitResponseDTO;
import com.messismo.bar.Entities.Benefit;
import com.messismo.bar.Services.BenefitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class BenefitControllerTests {

    @InjectMocks
    private BenefitController benefitController;

    @Mock
    private BenefitService benefitService;

    private BenefitResponseDTO discountBenefitResponse;
    private BenefitResponseDTO freeProductBenefitResponse;
    private BenefitRequestDTO discountBenefitRequest;
    private BenefitRequestDTO freeProductBenefitRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup discount benefit response
        discountBenefitResponse = BenefitResponseDTO.builder()
                .id(1L)
                .type(Benefit.BenefitType.DISCOUNT)
                .pointsRequired(100)
                .discountType(Benefit.DiscountType.PERCENTAGE)
                .discountValue(15.0)
                .applicableDays(Arrays.asList("MONDAY", "TUESDAY"))
                .createdBy("admin@test.com")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        // Setup free product benefit response
        freeProductBenefitResponse = BenefitResponseDTO.builder()
                .id(2L)
                .type(Benefit.BenefitType.FREE_PRODUCT)
                .pointsRequired(200)
                .productIds(Arrays.asList(1L, 2L, 3L))
                .createdBy("manager@test.com")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        // Setup discount benefit request
        discountBenefitRequest = BenefitRequestDTO.builder()
                .type(Benefit.BenefitType.DISCOUNT)
                .pointsRequired(100)
                .discountType(Benefit.DiscountType.PERCENTAGE)
                .discountValue(15.0)
                .applicableDays(Arrays.asList("MONDAY", "TUESDAY"))
                .build();

        // Setup free product benefit request
        freeProductBenefitRequest = BenefitRequestDTO.builder()
                .type(Benefit.BenefitType.FREE_PRODUCT)
                .pointsRequired(200)
                .productIds(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    @Test
    public void testGetAllBenefits_Success() throws Exception {
        // Arrange
        List<BenefitResponseDTO> benefits = Arrays.asList(discountBenefitResponse, freeProductBenefitResponse);
        when(benefitService.getAllActiveBenefits()).thenReturn(benefits);

        // Act
        ResponseEntity<?> response = benefitController.getAllBenefits();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(benefits, response.getBody());
        verify(benefitService, times(1)).getAllActiveBenefits();
    }

    @Test
    public void testGetAllBenefits_EmptyList() throws Exception {
        // Arrange
        when(benefitService.getAllActiveBenefits()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<?> response = benefitController.getAllBenefits();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList(), response.getBody());
        verify(benefitService, times(1)).getAllActiveBenefits();
    }



    @Test
    public void testGetBenefitById_Success() throws Exception {
        // Arrange
        when(benefitService.getBenefitById(1L)).thenReturn(Optional.of(discountBenefitResponse));

        // Act
        ResponseEntity<?> response = benefitController.getBenefitById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(discountBenefitResponse, response.getBody());
        verify(benefitService, times(1)).getBenefitById(1L);
    }

    @Test
    public void testGetBenefitById_NotFound() throws Exception {
        // Arrange
        when(benefitService.getBenefitById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<BenefitResponseDTO> response = benefitController.getBenefitById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // notFound().build() no tiene body
        verify(benefitService, times(1)).getBenefitById(999L);
    }



    @Test
    public void testCreateDiscountBenefit_Success() throws Exception {
        // Arrange
        when(benefitService.createBenefit(any(BenefitRequestDTO.class))).thenReturn(discountBenefitResponse);

        // Act
        ResponseEntity<?> response = benefitController.createBenefit(discountBenefitRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(discountBenefitResponse, response.getBody());
        verify(benefitService, times(1)).createBenefit(any(BenefitRequestDTO.class));
    }

    @Test
    public void testCreateFreeProductBenefit_Success() throws Exception {
        // Arrange
        when(benefitService.createBenefit(any(BenefitRequestDTO.class))).thenReturn(freeProductBenefitResponse);

        // Act
        ResponseEntity<?> response = benefitController.createBenefit(freeProductBenefitRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(freeProductBenefitResponse, response.getBody());
        verify(benefitService, times(1)).createBenefit(any(BenefitRequestDTO.class));
    }

    @Test
    public void testCreateBenefit_NullRequest() throws Exception {
        // Act - Con request null, el controller intenta acceder a los campos y se produce NullPointerException
        // que es capturada por el try-catch y retorna INTERNAL_SERVER_ERROR
        ResponseEntity<BenefitResponseDTO> response = benefitController.createBenefit(null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(benefitService, never()).createBenefit(any(BenefitRequestDTO.class));
    }

    @Test
    public void testCreateBenefit_ServiceException() throws Exception {
        // Arrange
        when(benefitService.createBenefit(any(BenefitRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act - El controller captura la excepción del service y retorna INTERNAL_SERVER_ERROR
        ResponseEntity<BenefitResponseDTO> response = benefitController.createBenefit(discountBenefitRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(benefitService, times(1)).createBenefit(any(BenefitRequestDTO.class));
    }

    @Test
    public void testDeleteBenefit_Success() throws Exception {
        // Arrange
        when(benefitService.deleteBenefit(1L)).thenReturn(true);

        // Act
        ResponseEntity<?> response = benefitController.deleteBenefit(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(benefitService, times(1)).deleteBenefit(1L);
    }

    @Test
    public void testDeleteBenefit_NotFound() throws Exception {
        // Arrange
        when(benefitService.deleteBenefit(999L)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = benefitController.deleteBenefit(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // notFound().build() no tiene body
        verify(benefitService, times(1)).deleteBenefit(999L);
    }



    @Test
    public void testGetBenefitsByType_Success() throws Exception {
        // Arrange
        List<BenefitResponseDTO> discountBenefits = Arrays.asList(discountBenefitResponse);
        when(benefitService.getBenefitsByType(Benefit.BenefitType.DISCOUNT)).thenReturn(discountBenefits);

        // Act
        ResponseEntity<?> response = benefitController.getBenefitsByType(Benefit.BenefitType.DISCOUNT);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(discountBenefits, response.getBody());
        verify(benefitService, times(1)).getBenefitsByType(Benefit.BenefitType.DISCOUNT);
    }

    @Test
    public void testGetBenefitsByType_EmptyList() throws Exception {
        // Arrange
        when(benefitService.getBenefitsByType(Benefit.BenefitType.DISCOUNT)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<?> response = benefitController.getBenefitsByType(Benefit.BenefitType.DISCOUNT);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList(), response.getBody());
        verify(benefitService, times(1)).getBenefitsByType(Benefit.BenefitType.DISCOUNT);
    }



    @Test
    public void testGetBenefitsForPoints_Success() throws Exception {
        // Arrange
        List<BenefitResponseDTO> availableBenefits = Arrays.asList(discountBenefitResponse);
        when(benefitService.getBenefitsForPoints(150)).thenReturn(availableBenefits);

        // Act
        ResponseEntity<?> response = benefitController.getBenefitsForPoints(150);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(availableBenefits, response.getBody());
        verify(benefitService, times(1)).getBenefitsForPoints(150);
    }

    @Test
    public void testGetBenefitsForPoints_NegativePoints() throws Exception {
        // Arrange - Con puntos negativos, el servicio retorna lista vacía
        when(benefitService.getBenefitsForPoints(-10)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<BenefitResponseDTO>> response = benefitController.getBenefitsForPoints(-10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(benefitService, times(1)).getBenefitsForPoints(-10);
    }


}