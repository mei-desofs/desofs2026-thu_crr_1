package com.techstore.app.repository;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UserId> {

    Optional<User> findByEmail(Email email);
    boolean existsByEmail(String email);

    boolean existsBySupabaseUserId(SupabaseUserId supabaseUserId);
    Optional<User> findBySupabaseUserId(SupabaseUserId supabaseUserId);

    User save(User user);
}
