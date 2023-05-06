package com.flapkap.challenge.repositories;

import com.flapkap.challenge.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findBySellerId(Long sellerId, Pageable page);
    Optional<Product> findByProductName(String productName);
    Optional<Product> findByProductNameAndIdNot(String productName, Long id);

}
