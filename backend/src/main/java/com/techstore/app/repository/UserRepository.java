package com.techstore.app.repository;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(Email email);

    Optional<User> findBySupabaseUserId(SupabaseUserId supabaseUserId);

    Optional<User> findBySupabaseUserId(String supabaseUserId);

    User save(User user);
}
