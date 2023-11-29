package com.demo.orders.repo.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor

public class Order {

    @Id
    private String orderId;
    private String orderName;
    private Long createDate;
    private Long updateDate;
}
