package com.devmart.product_service.repository;

import com.devmart.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRespository extends MongoRepository<Product, String> {
}
