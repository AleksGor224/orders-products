package com.demo.orders.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewDto {
    private String productId;
    private String productName;
    private Double productPrice;
    private String createDate;
    private String updateDate;
}
