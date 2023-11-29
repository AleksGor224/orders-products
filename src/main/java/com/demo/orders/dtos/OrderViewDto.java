package com.demo.orders.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderViewDto {
    private String orderId;
    private String orderName;
    private List<ProductViewDto> products;
    private String createDate;
    private String updateDate;
}
