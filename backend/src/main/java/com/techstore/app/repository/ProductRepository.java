package com.techstore.app.repository;

import com.techstore.app.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByName(String name);

    @Query("SELECT p FROM Product p WHERE upper(p.name) LIKE upper(concat('%', :productName, '%'))")
    Page<Product> findByNameLike(String productName, Pageable pageable);
}
