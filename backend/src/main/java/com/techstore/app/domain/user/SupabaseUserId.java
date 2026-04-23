package com.techstore.app.domain.user;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Embeddable
public class SupabaseUserId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected SupabaseUserId() {
        // For JPA
    }

    public SupabaseUserId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Supabase User ID cannot be null.");
        }
        this.id = id;
    }

    public static SupabaseUserId newId() {
        return new SupabaseUserId(UUID.randomUUID());
    }

    public static SupabaseUserId fromString(String value) {
        return new SupabaseUserId(UUID.fromString(value));
    }
}
