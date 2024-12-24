package com.devmart.product_service.service;

import com.devmart.product_service.dto.ProductRequest;
import com.devmart.product_service.dto.ProductResponse;
import com.devmart.product_service.model.Product;
import com.devmart.product_service.repository.ProductRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRespository productRespository;

    public void createProduct(ProductRequest prodRequest) {
        Product product = Product.builder()
                            .name(prodRequest.getName())
                            .description(prodRequest.getDescription())
                            .price(prodRequest.getPrice()).build();

        productRespository.save(product);
        log.info("Product {} is saved", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> allProducts = productRespository.findAll();

        return allProducts.stream().map(product -> mapToProductResponse(product)).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}
