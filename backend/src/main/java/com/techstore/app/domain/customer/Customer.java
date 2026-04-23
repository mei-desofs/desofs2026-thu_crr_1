package com.techstore.app.domain.customer;

import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "customers")
public class Customer {

    @EmbeddedId
    private CustomerId id;

    @Embedded
    @Column(nullable = false, unique = true)
    private Nif nif;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Customer() {
        // For JPA
    }

    public Customer(Nif nif, User user) {
        this.id = CustomerId.newId();
        if (nif == null) {
            throw new BusinessException("NIF cannot be null.");
        }
        if (user == null) {
            throw new BusinessException("User cannot be null.");
        }
        this.nif = nif;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
