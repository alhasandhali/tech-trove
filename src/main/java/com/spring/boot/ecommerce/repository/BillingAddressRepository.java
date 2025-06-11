package com.spring.boot.ecommerce.repository;

import com.spring.boot.ecommerce.model.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {
}
