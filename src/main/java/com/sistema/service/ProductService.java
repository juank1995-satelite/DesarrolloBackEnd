package com.sistema.service;

import com.sistema.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Product create(Product product);
    Page<Product> findAllActive(Pageable pageable);
    Product findById(Long id);
    Product update(Product product);
    Product updateStock(Long id, Integer quantityChange);
    void deleteLogically(Long id);
    Product reactivate(Long id);
}
