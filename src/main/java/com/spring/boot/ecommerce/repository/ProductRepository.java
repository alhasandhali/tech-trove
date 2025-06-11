package com.spring.boot.ecommerce.repository;

import com.spring.boot.ecommerce.model.Brand;
import com.spring.boot.ecommerce.model.Category;
import com.spring.boot.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByBrand(Brand brand);
    List<Product> findByNameContainingIgnoreCase(String name);
    Product getProductById(Long id);
}
