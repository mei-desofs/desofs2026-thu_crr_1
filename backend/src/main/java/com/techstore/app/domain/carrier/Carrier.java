package com.techstore.app.domain.carrier;

import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.customer.Nif;
import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "carriers")
public class Carrier {
    @EmbeddedId
    private CarrierId id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Carrier() {
        // For JPA
    }

    public Carrier(User user) {
        this.id = CarrierId.newId();
        if (user == null) {
            throw new BusinessException("User cannot be null.");
        }
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
