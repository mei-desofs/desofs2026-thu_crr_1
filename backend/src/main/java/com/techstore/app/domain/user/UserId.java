package com.techstore.app.domain.user;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Embeddable
public class UserId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected UserId() {
        // For JPA
    }

    public UserId(UUID id) {
        this.id = Objects.requireNonNull(id, "UserId cannot be null.");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId fromString(String value) {
        return new UserId(UUID.fromString(value));
    }
}
