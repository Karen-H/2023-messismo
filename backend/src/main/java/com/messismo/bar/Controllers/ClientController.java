package com.messismo.bar.Controllers;

import com.messismo.bar.DTOs.ClientProfileDTO;
import com.messismo.bar.DTOs.ProductClientViewDTO;
import com.messismo.bar.Services.ProductService;
import com.messismo.bar.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/client")
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('CLIENT')")
public class ClientController {

    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/products")
    public ResponseEntity<?> getProductsForClient() {
        try {
            // Solo devolver información básica de productos para clientes
            List<ProductClientViewDTO> clientProducts = productService.getAllProducts()
                    .stream()
                    .map(product -> ProductClientViewDTO.builder()
                            .name(product.getName())
                            .description(product.getDescription())
                            .unitPrice(product.getUnitPrice())
                            .category(product.getCategory().getName())
                            .build())
                    .collect(Collectors.toList());
            
            return ResponseEntity.status(HttpStatus.OK).body(clientProducts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving products");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getClientProfile(Authentication authentication) {
        try {
            String email = authentication.getName(); // El email es el username en Spring Security
            ClientProfileDTO profile = userService.getClientProfile(email);
            return ResponseEntity.status(HttpStatus.OK).body(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving profile");
        }
    }
}