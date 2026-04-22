package com.techstore.app.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.techstore.app.exception.BusinessException;

import java.time.LocalDateTime;

/**
 * Entity representing a user in the system.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class User {

    @EmbeddedId
    private UserId id;

    @Version
    private Long version;

    @Embedded
    private Email email;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "supabase_user_id", nullable = false, unique = true))
    private SupabaseUserId supabaseUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public User(Email email, Role role, SupabaseUserId supabaseUserId) {
        if (email == null) {
            throw new BusinessException("Email cannot be null.");
        }
        if (role == null) {
            throw new BusinessException("Role cannot be null.");
        }
        if (supabaseUserId == null) {
            throw new BusinessException("Supabase User ID cannot be null.");
        }
        this.email = email;
        this.role = role;
        this.supabaseUserId = supabaseUserId;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}