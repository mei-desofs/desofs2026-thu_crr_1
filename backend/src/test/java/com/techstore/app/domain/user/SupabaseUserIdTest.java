package com.techstore.app.domain.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SupabaseUserIdTest {

    @Test
    void shouldCreateWithUuid() {
        UUID uuid = UUID.randomUUID();

        SupabaseUserId id = new SupabaseUserId(uuid);

        assertEquals(uuid, id.getId());
    }

    @Test
    void newIdGeneratesNonNullUuid() {
        SupabaseUserId id = SupabaseUserId.newId();

        assertNotNull(id);
        assertNotNull(id.getId());
    }

    @Test
    void fromStringParsesUuid() {
        UUID uuid = UUID.randomUUID();

        SupabaseUserId id = SupabaseUserId.fromString(uuid.toString());

        assertEquals(uuid, id.getId());
    }

    @Test
    void fromStringThrowsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> SupabaseUserId.fromString("not-a-uuid"));
    }

    @Test
    void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new SupabaseUserId(null));
    }

    @Test
    void equalsAndHashCodeUseValue() {
        UUID uuid = UUID.randomUUID();
        SupabaseUserId a = new SupabaseUserId(uuid);
        SupabaseUserId b = new SupabaseUserId(uuid);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
