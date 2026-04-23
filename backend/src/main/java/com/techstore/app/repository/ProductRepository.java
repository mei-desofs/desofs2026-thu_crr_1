package com.techstore.app.repository;

import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductId;
import com.techstore.app.domain.product.ProductName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, ProductId> {

    List<Product> findByName(ProductName name);

    @Query("SELECT p FROM Product p WHERE upper(p.name) LIKE upper(concat('%', :productName, '%'))")
    Page<Product> findByNameLike(ProductName productName, Pageable pageable);
}
