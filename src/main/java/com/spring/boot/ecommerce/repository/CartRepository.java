package com.spring.boot.ecommerce.repository;

import com.spring.boot.ecommerce.model.Cart;
import com.spring.boot.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    @Query("SELECT c FROM Cart c JOIN FETCH c.items WHERE c.user.id = :userId")
    Cart findByUserIdWithItems(@Param("userId") Long userId);
}
