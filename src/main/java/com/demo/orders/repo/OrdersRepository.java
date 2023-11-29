package com.demo.orders.repo;

import com.demo.orders.repo.entities.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends MongoRepository<Order, String> {
}
