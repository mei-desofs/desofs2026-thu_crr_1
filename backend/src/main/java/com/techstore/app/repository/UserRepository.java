package com.techstore.app.repository;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(Email email);

    User save(User user);
}
