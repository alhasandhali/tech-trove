package com.spring.boot.ecommerce.repository;

import com.spring.boot.ecommerce.model.Product;
import com.spring.boot.ecommerce.model.Wishlist;
import com.spring.boot.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser(User user);

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.products WHERE w.id = :id")
    Optional<Wishlist> findByIdWithProducts(@Param("id") Long id);

    @Query("SELECT w FROM Wishlist w JOIN w.products p WHERE p = :product")
    List<Wishlist> findAllByProduct(@Param("product") Product product);
}
