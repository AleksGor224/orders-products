package com.demo.orders.repo.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@CompoundIndex(def = "{'orderId': 1, 'productId': 1}", unique = true)
public class LinkOrderProducts {

    @Id
    private String id;
    private String orderId;
    private String productId;
}
