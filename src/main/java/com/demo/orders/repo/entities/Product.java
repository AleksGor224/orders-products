package com.demo.orders.repo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
public class Product {

    @Id
    private String productId;
    private String productName;
    private Double productPrice;
    private Long createDate;
    private Long updateDate;
}
