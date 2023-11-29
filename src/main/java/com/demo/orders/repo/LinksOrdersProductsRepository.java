package com.demo.orders.repo;

import com.demo.orders.repo.entities.LinkOrderProducts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinksOrdersProductsRepository extends MongoRepository<LinkOrderProducts, String> {

    List<LinkOrderProducts> findByOrderId(String orderId);

    Optional<LinkOrderProducts> findByOrderIdAndProductId(String orderId, String productId);

    Optional<LinkOrderProducts> findByProductId(String productId);

    void deleteByProductId(String productId);

    void deleteByOrderId(String orderId);
}
