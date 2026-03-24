package com.techstore.app.domain.category;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Category() {}

    public Category(String name) {
        if (!isValid(name)) {
            throw new BusinessException("Category name must contain only letters.");
        }

        this.name = name;
    }

    public boolean isValid(String name) {
        return name.matches("[a-zA-Z0-9 ]+") && !name.isBlank();
    }
}
