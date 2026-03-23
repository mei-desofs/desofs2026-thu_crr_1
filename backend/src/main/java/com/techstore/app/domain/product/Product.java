package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    private Money price;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    public Product() {}

    public Product(String name, String description, Money price, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

}
