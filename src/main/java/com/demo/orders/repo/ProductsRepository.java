package com.demo.orders.repo;

import com.demo.orders.repo.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends MongoRepository<Product, String> {

    List<Product> findByProductIdIn(List<String> ids);
}
