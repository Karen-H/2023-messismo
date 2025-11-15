package com.messismo.bar.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductClientViewDTO {
    private String name;
    private String description;
    private Double unitPrice;
    private String category;
}