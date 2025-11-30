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

        freeProductBenefitResponse = BenefitResponseDTO.builder()
                .id(2L)
                .type(Benefit.BenefitType.FREE_PRODUCT)
                .pointsRequired(200)
                .productIds(Arrays.asList(1L, 2L, 3L))
                .createdBy("manager@test.com")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        discountBenefitRequest = BenefitRequestDTO.builder()
                .type(Benefit.BenefitType.DISCOUNT)
                .pointsRequired(100)
                .discountType(Benefit.DiscountType.PERCENTAGE)
                .discountValue(15.0)
                .applicableDays(Arrays.asList("MONDAY", "TUESDAY"))
                .build();

        freeProductBenefitRequest = BenefitRequestDTO.builder()
                .type(Benefit.BenefitType.FREE_PRODUCT)
                .pointsRequired(200)
                .productIds(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    @Test
    public void testGetAllBenefits_Success() throws Exception {
        List<BenefitResponseDTO> benefits = Arrays.asList(discountBenefitResponse, freeProductBenefitResponse);
        when(benefitService.getAllActiveBenefits()).thenReturn(benefits);

        ResponseEntity<?> response = benefitController.getAllBenefits();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(benefits, response.getBody());
        verify(benefitService, times(1)).getAllActiveBenefits();
    }

    @Test
    public void testGetAllBenefits_EmptyList() throws Exception {
        when(benefitService.getAllActiveBenefits()).thenReturn(Arrays.asList());

        ResponseEntity<?> response = benefitController.getAllBenefits();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList(), response.getBody());
        verify(benefitService, times(1)).getAllActiveBenefits();
    }



    @Test
    public void testGetBenefitById_Success() throws Exception {
        when(benefitService.getBenefitById(1L)).thenReturn(Optional.of(discountBenefitResponse));

        ResponseEntity<?> response = benefitController.getBenefitById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(discountBenefitResponse, response.getBody());
        verify(benefitService, times(1)).getBenefitById(1L);
    }

    @Test
    public void testGetBenefitById_NotFound() throws Exception {
        when(benefitService.getBenefitById(999L)).thenReturn(Optional.empty());

        ResponseEntity<BenefitResponseDTO> response = benefitController.getBenefitById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // notFound().build() no tiene body
        verify(benefitService, times(1)).getBenefitById(999L);
    }



    @Test
    public void testCreateDiscountBenefit_Success() throws Exception {
        when(benefitService.createBenefit(any(BenefitRequestDTO.class))).thenReturn(discountBenefitResponse);

        ResponseEntity<?> response = benefitController.createBenefit(discountBenefitRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(discountBenefitResponse, response.getBody());
        verify(benefitService, times(1)).createBenefit(any(BenefitRequestDTO.class));
    }



    @Test
    public void testCreateBenefit_NullRequest() throws Exception {
        ResponseEntity<BenefitResponseDTO> response = benefitController.createBenefit(null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(benefitService, never()).createBenefit(any(BenefitRequestDTO.class));
    }

    @Test
    public void testCreateBenefit_ServiceException() throws Exception {
        when(benefitService.createBenefit(any(BenefitRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<BenefitResponseDTO> response = benefitController.createBenefit(discountBenefitRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(benefitService, times(1)).createBenefit(any(BenefitRequestDTO.class));
    }

    @Test
    public void testDeleteBenefit_Success() throws Exception {
        when(benefitService.deleteBenefit(1L)).thenReturn(true);

        ResponseEntity<?> response = benefitController.deleteBenefit(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(benefitService, times(1)).deleteBenefit(1L);
    }

    @Test
    public void testDeleteBenefit_NotFound() throws Exception {
        when(benefitService.deleteBenefit(999L)).thenReturn(false);

        ResponseEntity<Void> response = benefitController.deleteBenefit(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // notFound().build() no tiene body
        verify(benefitService, times(1)).deleteBenefit(999L);
    }



    @Test
    public void testGetBenefitsByType_Success() throws Exception {
        List<BenefitResponseDTO> discountBenefits = Arrays.asList(discountBenefitResponse);
        when(benefitService.getBenefitsByType(Benefit.BenefitType.DISCOUNT)).thenReturn(discountBenefits);

        ResponseEntity<?> response = benefitController.getBenefitsByType(Benefit.BenefitType.DISCOUNT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(discountBenefits, response.getBody());
        verify(benefitService, times(1)).getBenefitsByType(Benefit.BenefitType.DISCOUNT);
    }

    @Test
    public void testGetBenefitsByType_EmptyList() throws Exception {
        when(benefitService.getBenefitsByType(Benefit.BenefitType.DISCOUNT)).thenReturn(Arrays.asList());

        ResponseEntity<?> response = benefitController.getBenefitsByType(Benefit.BenefitType.DISCOUNT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList(), response.getBody());
        verify(benefitService, times(1)).getBenefitsByType(Benefit.BenefitType.DISCOUNT);
    }



    @Test
    public void testGetBenefitsForPoints_Success() throws Exception {
        List<BenefitResponseDTO> availableBenefits = Arrays.asList(discountBenefitResponse);
        when(benefitService.getBenefitsForPoints(150)).thenReturn(availableBenefits);

        ResponseEntity<?> response = benefitController.getBenefitsForPoints(150.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(availableBenefits, response.getBody());
        verify(benefitService, times(1)).getBenefitsForPoints(150);
    }

    @Test
    public void testGetBenefitsForPoints_NegativePoints() throws Exception {
        when(benefitService.getBenefitsForPoints(-10)).thenReturn(Collections.emptyList());

        ResponseEntity<List<BenefitResponseDTO>> response = benefitController.getBenefitsForPoints(-10.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(benefitService, times(1)).getBenefitsForPoints(-10);
    }

    @Test
    public void testCheckDuplicateBenefit_IsDuplicate() throws Exception {
        when(benefitService.isDuplicateBenefit(any(BenefitRequestDTO.class))).thenReturn(true);

        ResponseEntity<Boolean> response = benefitController.checkDuplicateBenefit(discountBenefitRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        
        verify(benefitService, times(1)).isDuplicateBenefit(discountBenefitRequest);
    }

    @Test
    public void testCheckDuplicateBenefit_IsNotDuplicate() throws Exception {
        when(benefitService.isDuplicateBenefit(any(BenefitRequestDTO.class))).thenReturn(false);

        ResponseEntity<Boolean> response = benefitController.checkDuplicateBenefit(freeProductBenefitRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        
        verify(benefitService, times(1)).isDuplicateBenefit(freeProductBenefitRequest);
    }

    @Test
    public void testCheckDuplicateBenefit_ServiceException() throws Exception {
        when(benefitService.isDuplicateBenefit(any(BenefitRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Boolean> response = benefitController.checkDuplicateBenefit(discountBenefitRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(benefitService, times(1)).isDuplicateBenefit(discountBenefitRequest);
    }

}
